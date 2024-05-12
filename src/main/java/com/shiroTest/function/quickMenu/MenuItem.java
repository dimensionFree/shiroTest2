package com.shiroTest.function.quickMenu;

public enum MenuItem implements BaseCodeEnum {

    //add type handler
    PART_A(0),
    PART_B(1),
    PART_C(2);

    private int index;

    MenuItem(int code){
        this.index = code;
    }

    @Override
    public int getCode() {
        return index;
    }
}
