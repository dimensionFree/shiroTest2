package com.shiroTest.function.assistant.service;

import java.util.Set;

public interface IRecordIgnoreIpService {
    Set<String> listIgnoredIps();

    void addIgnoredIp(String ip);

    void removeIgnoredIp(String ip);

    boolean shouldIgnore(String ip);
}

