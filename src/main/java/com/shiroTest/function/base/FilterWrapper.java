package com.shiroTest.function.base;

import lombok.Data;

import java.util.Map;

@Data
public class FilterWrapper {
    private Map<String, Object> filters; // 用于筛选条件
    private String sortBy; // 用于排序的字段
    private Boolean ascending; // 是否升序
}

