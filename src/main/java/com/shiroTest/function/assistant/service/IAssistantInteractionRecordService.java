package com.shiroTest.function.assistant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.shiroTest.function.assistant.model.AssistantInteractionRecord;

import java.util.Map;
import java.time.LocalDate;

public interface IAssistantInteractionRecordService extends IService<AssistantInteractionRecord> {
    void recordInteraction(String interactionType,
                           String interactionAction,
                           Map<String, Object> interactionPayload,
                           String clientIp,
                           String userId,
                           String userAgent);

    PageInfo<AssistantInteractionRecord> getManageRecords(int currentPage,
                                                          int pageSize,
                                                          String interactionType,
                                                          String interactionAction,
                                                          LocalDate startDate,
                                                          LocalDate endDate);

    int flushAllCachedInteractionsToDb();
}
