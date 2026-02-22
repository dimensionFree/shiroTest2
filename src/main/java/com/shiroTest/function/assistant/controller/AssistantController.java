package com.shiroTest.function.assistant.controller;

import com.shiroTest.common.Result;
import com.shiroTest.function.assistant.model.AssistantContextResponse;
import com.shiroTest.function.assistant.model.AssistantInteractionRequest;
import com.shiroTest.function.assistant.service.IAssistantService;
import com.shiroTest.function.assistant.service.IAssistantInteractionRecordService;
import com.shiroTest.function.user.model.User4Display;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final IAssistantService assistantService;
    private final IAssistantInteractionRecordService assistantInteractionRecordService;

    public AssistantController(IAssistantService assistantService,
                               IAssistantInteractionRecordService assistantInteractionRecordService) {
        this.assistantService = assistantService;
        this.assistantInteractionRecordService = assistantInteractionRecordService;
    }

    @GetMapping("/context")
    public Result getContext(HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        AssistantContextResponse response = assistantService.getContextByIp(clientIp);
        return Result.success(response);
    }

    @PostMapping("/interaction")
    public Result recordInteraction(@RequestBody AssistantInteractionRequest requestBody, HttpServletRequest request) {
        if (requestBody == null) {
            return Result.fail("request body cannot be null");
        }
        if (isBlank(requestBody.getInteractionType())) {
            return Result.fail("interactionType cannot be blank");
        }
        if (isBlank(requestBody.getInteractionAction())) {
            return Result.fail("interactionAction cannot be blank");
        }
        assistantInteractionRecordService.recordInteraction(
                requestBody.getInteractionType().trim(),
                requestBody.getInteractionAction().trim(),
                requestBody.getInteractionPayload(),
                extractClientIp(request),
                extractCurrentUserId(),
                request == null ? null : request.getHeader("User-Agent")
        );
        return Result.success(true);
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

    private String extractCurrentUserId() {
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof User4Display) {
                return ((User4Display) principal).getId();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
