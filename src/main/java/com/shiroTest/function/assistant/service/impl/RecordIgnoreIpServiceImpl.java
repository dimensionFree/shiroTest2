package com.shiroTest.function.assistant.service.impl;

import com.shiroTest.function.assistant.service.IRecordIgnoreIpService;
import com.shiroTest.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@Service
public class RecordIgnoreIpServiceImpl implements IRecordIgnoreIpService {

    private static final String IGNORE_IP_SET_KEY = "RECORD_IGNORE_IP_SET";
    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");
    private static final Pattern IPV6_PATTERN =
            Pattern.compile("^[0-9a-fA-F:]+$");

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Set<String> listIgnoredIps() {
        Set<Object> rawMembers = redisUtil.sMembers(IGNORE_IP_SET_KEY);
        if (rawMembers == null || rawMembers.isEmpty()) {
            return Collections.emptySet();
        }
        TreeSet<String> result = new TreeSet<>();
        for (Object member : rawMembers) {
            if (member == null) {
                continue;
            }
            String normalized = normalize(member.toString());
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    @Override
    public void addIgnoredIp(String ip) {
        String normalizedIp = normalize(ip);
        if (normalizedIp == null || !isValidIp(normalizedIp)) {
            throw new IllegalArgumentException("invalid ip format");
        }
        redisUtil.sAdd(IGNORE_IP_SET_KEY, normalizedIp);
    }

    @Override
    public void removeIgnoredIp(String ip) {
        String normalizedIp = normalize(ip);
        if (normalizedIp == null) {
            return;
        }
        redisUtil.sRemove(IGNORE_IP_SET_KEY, normalizedIp);
    }

    @Override
    public boolean shouldIgnore(String ip) {
        String normalizedIp = normalize(ip);
        if (normalizedIp == null) {
            return false;
        }
        return redisUtil.sIsMember(IGNORE_IP_SET_KEY, normalizedIp);
    }

    private String normalize(String ip) {
        String normalized = StringUtils.trimToNull(ip);
        if (normalized == null) {
            return null;
        }
        return normalized.toLowerCase();
    }

    private boolean isValidIp(String ip) {
        if ("localhost".equalsIgnoreCase(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        return IPV4_PATTERN.matcher(ip).matches() || IPV6_PATTERN.matcher(ip).matches();
    }
}
