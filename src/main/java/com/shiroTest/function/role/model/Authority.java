package com.shiroTest.function.role.model;

import com.shiroTest.function.quickMenu.BaseCodeEnum;

public enum Authority implements BaseCodeEnum {
    ALL(0),
    USER_READ(1),
    USER_EDIT(2),
    ARTICLE_READ(3),
    ARTICLE_EDIT(4),
    USER_EDIT_SELF(5),
    USER_READ_SELF(6),
    ARTICLE_EDIT_SELF(7),
    ARTICLE_CREATE(8),
    ;
    //todo: add read/edit self

    private int index;

    Authority(int code){
        this.index = code;
    }

    @Override
    public int getCode() {
        return index;
    }



}
