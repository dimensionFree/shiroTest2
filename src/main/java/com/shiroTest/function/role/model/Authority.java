package com.shiroTest.function.role.model;

import com.shiroTest.function.quickMenu.BaseCodeEnum;

public enum Authority implements BaseCodeEnum {
    ALL(0),
    USER_READ(1),
    USER_EDIT(2),
    ARTICLE_READ(3),
    ARTICLE_EDIT(4),
    USER_EDIT_SELF(5),
    ;

    private int index;

    Authority(int code){
        this.index = code;
    }

    @Override
    public int getCode() {
        return index;
    }



}
