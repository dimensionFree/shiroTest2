package com.shiroTest.function.assistant.service.impl;

import com.shiroTest.function.assistant.dao.AssistantInteractionRecordMapper;
import com.shiroTest.function.assistant.model.AssistantInteractionRecord;
import com.shiroTest.function.assistant.model.GeoContext;
import com.shiroTest.function.assistant.service.AssistantRemoteClient;
import com.shiroTest.utils.JsonUtil;
import com.shiroTest.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantInteractionRecordServiceImplTest {

    @Mock
    private AssistantInteractionRecordMapper assistantInteractionRecordMapper;

    @Mock
    private AssistantRemoteClient assistantRemoteClient;

    @Mock
    private RedisUtil redisUtil;

    private AssistantInteractionRecordServiceImpl assistantInteractionRecordService;

    @BeforeEach
    void setUp() {
        assistantInteractionRecordService = new AssistantInteractionRecordServiceImpl();
        ReflectionTestUtils.setField(assistantInteractionRecordService, "baseMapper", assistantInteractionRecordMapper);
        ReflectionTestUtils.setField(assistantInteractionRecordService, "assistantRemoteClient", assistantRemoteClient);
        ReflectionTestUtils.setField(assistantInteractionRecordService, "redisUtil", redisUtil);
    }

    @Test
    void recordInteraction_should_cache_private_network_location_for_private_ip() {
        assistantInteractionRecordService.recordInteraction(
                "AVATAR",
                "tap",
                Map.of("distance", 1),
                "10.10.0.2",
                null,
                "UA-1"
        );

        verify(redisUtil).hPut(anyString(), anyString(), anyString());
        verify(redisUtil).expire(anyString(), anyLong(), any(TimeUnit.class));
        verify(assistantInteractionRecordMapper, never()).insert(any());
        verify(assistantRemoteClient, never()).fetchGeoContext(any());
    }

    @Test
    void recordInteraction_should_cache_geo_fields_for_public_ip() {
        doReturn(new GeoContext("Japan", "Tokyo", "Shinjuku", null, null))
                .when(assistantRemoteClient).fetchGeoContext("8.8.8.8");

        assistantInteractionRecordService.recordInteraction(
                "AVATAR",
                "flick",
                Map.of("direction", "left"),
                "8.8.8.8",
                "user-id-1",
                "UA-2"
        );

        verify(redisUtil).hPut(anyString(), anyString(), anyString());
        verify(assistantInteractionRecordMapper, never()).insert(any());
    }

    @Test
    void recordInteraction_should_bypass_aggregation_for_chat_type() {
        doReturn(new GeoContext("Japan", "Tokyo", "Minato", null, null))
                .when(assistantRemoteClient).fetchGeoContext("8.8.4.4");

        assistantInteractionRecordService.recordInteraction(
                "CHAT",
                "message_send",
                Map.of("text", "hello"),
                "8.8.4.4",
                "user-id-2",
                "UA-3"
        );

        verify(assistantInteractionRecordMapper).insert(any(AssistantInteractionRecord.class));
        verify(redisUtil, never()).hPut(anyString(), anyString(), anyString());
    }

    @Test
    void flushAggregatedInteractionsToDb_should_persist_expired_bucket() {
        String key = "ASSISTANT_INTERACTION_AGG_200001010000";
        Map<String, Object> cacheValue = new HashMap<>();
        cacheValue.put("interactionType", "AVATAR");
        cacheValue.put("interactionAction", "tap");
        cacheValue.put("interactionPayload", JsonUtil.toJson(Map.of("distance", 1)));
        cacheValue.put("clientIp", "10.0.0.1");
        cacheValue.put("clientIpLocation", "PRIVATE_NETWORK");
        cacheValue.put("clientIpCountry", "PRIVATE_NETWORK");
        cacheValue.put("clientIpProvince", "PRIVATE_NETWORK");
        cacheValue.put("clientIpCity", "PRIVATE_NETWORK");
        cacheValue.put("userId", null);
        cacheValue.put("userAgent", "UA-4");
        cacheValue.put("triggerTime", LocalDateTime.now().toString());

        when(redisUtil.keys("ASSISTANT_INTERACTION_AGG_*")).thenReturn(Set.of(key));
        when(redisUtil.hGetAll(key)).thenReturn(Map.of("10.0.0.1|AVATAR|tap", JsonUtil.toJson(cacheValue)));

        assistantInteractionRecordService.flushAggregatedInteractionsToDb();

        verify(redisUtil).delete(key);
    }

    @Test
    void recordInteraction_should_throw_when_action_blank() {
        assertThatThrownBy(() -> assistantInteractionRecordService.recordInteraction(
                "AVATAR",
                " ",
                null,
                "1.1.1.1",
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("interactionAction");
    }
}
