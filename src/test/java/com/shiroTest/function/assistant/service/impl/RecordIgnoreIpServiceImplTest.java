package com.shiroTest.function.assistant.service.impl;

import com.shiroTest.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordIgnoreIpServiceImplTest {

    @Mock
    private RedisUtil redisUtil;

    private RecordIgnoreIpServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RecordIgnoreIpServiceImpl();
        ReflectionTestUtils.setField(service, "redisUtil", redisUtil);
    }

    @Test
    void addIgnoredIp_should_add_when_ipv4_valid() {
        service.addIgnoredIp("10.0.0.1");

        verify(redisUtil).sAdd("RECORD_IGNORE_IP_SET", "10.0.0.1");
    }

    @Test
    void addIgnoredIp_should_throw_when_ip_invalid() {
        assertThatThrownBy(() -> service.addIgnoredIp("invalid-ip"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid ip");
    }

    @Test
    void listIgnoredIps_should_return_sorted_values() {
        when(redisUtil.sMembers("RECORD_IGNORE_IP_SET")).thenReturn(Set.of("127.0.0.1", "10.0.0.2"));

        Set<String> result = service.listIgnoredIps();

        assertThat(result).containsExactly("10.0.0.2", "127.0.0.1");
    }
}

