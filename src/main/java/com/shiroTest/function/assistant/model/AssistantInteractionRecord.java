package com.shiroTest.function.assistant.model;

import com.shiroTest.function.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssistantInteractionRecord extends BaseEntity {
    private String interactionType;
    private String interactionAction;
    private String interactionPayload;

    private String clientIp;
    private String clientIpLocation;
    private String clientIpCountry;
    private String clientIpProvince;
    private String clientIpCity;

    private String userId;
    private String userAgent;
    private LocalDateTime triggerTime;
}
