package com.shiroTest.function.assistant.service;

import com.shiroTest.function.assistant.model.AssistantContextResponse;

public interface IAssistantService {
    AssistantContextResponse getContextByIp(String clientIp);
}
