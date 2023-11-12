package com.shiroTest.common;

import com.shiroTest.enums.ResultCodeEnum;

public class MyException extends Exception{
    private ResultCodeEnum errorEnum;

    public ResultCodeEnum getErrorEnum() {
        return errorEnum;
    }

    public MyException(ResultCodeEnum error) {
        this.errorEnum = error;
    }

    public MyException(ResultCodeEnum error, String message) {
        super(message);
        this.errorEnum = error;
    }
}
