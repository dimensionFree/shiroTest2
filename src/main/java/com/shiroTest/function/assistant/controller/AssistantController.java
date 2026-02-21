package com.shiroTest.function.assistant.controller;

import com.shiroTest.common.Result;
import com.shiroTest.function.assistant.model.AssistantContextResponse;
import com.shiroTest.function.assistant.service.IAssistantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final IAssistantService assistantService;

    public AssistantController(IAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/context")
    public Result getContext(HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        AssistantContextResponse response = assistantService.getContextByIp(clientIp);
        return Result.success(response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] parts = xForwardedFor.split(",");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
