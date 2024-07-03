package com.shiroTest.function.role.model;

import com.shiroTest.function.quickMenu.BaseCodeEnum;

public enum Feature implements BaseCodeEnum {
    ALL(0),
    USER(1),
    ARTICLE(2),
    COMMENT(3)
    ;

    private int index;

    Feature(int code){
        this.index = code;
    }

    @Override
    public int getCode() {
        return index;
    }



}
