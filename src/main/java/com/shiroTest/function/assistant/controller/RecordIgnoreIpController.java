package com.shiroTest.function.assistant.controller;

import com.shiroTest.common.Result;
import com.shiroTest.function.assistant.service.IRecordIgnoreIpService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/record/manage/ignore-ip")
public class RecordIgnoreIpController {

    private final IRecordIgnoreIpService recordIgnoreIpService;

    public RecordIgnoreIpController(IRecordIgnoreIpService recordIgnoreIpService) {
        this.recordIgnoreIpService = recordIgnoreIpService;
    }

    @GetMapping("/list")
    public Result listIgnoredIps() {
        return Result.success(recordIgnoreIpService.listIgnoredIps());
    }

    @PostMapping("/add")
    public Result addIgnoredIp(@RequestBody Map<String, String> requestBody) {
        if (requestBody == null) {
            return Result.fail("request body cannot be null");
        }
        String ip = requestBody.get("ip");
        if (ip == null || ip.trim().isEmpty()) {
            return Result.fail("ip cannot be blank");
        }
        try {
            recordIgnoreIpService.addIgnoredIp(ip);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
        return Result.success(true);
    }

    @DeleteMapping("/remove")
    public Result removeIgnoredIp(@RequestParam("ip") String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return Result.fail("ip cannot be blank");
        }
        recordIgnoreIpService.removeIgnoredIp(ip);
        return Result.success(true);
    }
}
