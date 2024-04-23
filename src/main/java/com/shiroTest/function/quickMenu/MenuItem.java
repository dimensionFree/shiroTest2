package com.shiroTest.function.quickMenu;

public enum MenuItem {
    PART_A(),
    PART_B,
    PART_C;

    private int index;

    MenuItem(){
        this.index=ordinal();
    }
}
