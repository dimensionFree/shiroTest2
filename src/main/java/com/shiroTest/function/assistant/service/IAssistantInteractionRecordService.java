package com.shiroTest.function.assistant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiroTest.function.assistant.model.AssistantInteractionRecord;

import java.util.Map;

public interface IAssistantInteractionRecordService extends IService<AssistantInteractionRecord> {
    void recordInteraction(String interactionType,
                           String interactionAction,
                           Map<String, Object> interactionPayload,
                           String clientIp,
                           String userId,
                           String userAgent);
}
