package com.shiroTest.function.assistant.model;

import lombok.Data;

import java.util.Map;

@Data
public class AssistantInteractionRequest {
    private String interactionType;
    private String interactionAction;
    private Map<String, Object> interactionPayload;
}
