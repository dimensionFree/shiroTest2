package com.shiroTest.function.role.model;

import com.shiroTest.function.quickMenu.BaseCodeEnum;

public enum Authority implements BaseCodeEnum {
    ALL(0),
    READ_USER(1),
    WRITE_USER(2),
    READ_ARTICLE(3),
    WRITE_ARTICLE(4);

    private int index;

    Authority(int code){
        this.index = code;
    }

    @Override
    public int getCode() {
        return index;
    }
}
