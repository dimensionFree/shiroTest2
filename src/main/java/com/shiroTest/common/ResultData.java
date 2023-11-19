package com.shiroTest.common;

import lombok.Data;

import java.io.Serializable;
@Data
public class ResultData implements Serializable {
    private String code;
    private String message;
    private Object data;
}
