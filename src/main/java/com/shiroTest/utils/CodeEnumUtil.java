package com.shiroTest.utils;

import com.shiroTest.function.quickMenu.BaseCodeEnum;

public class CodeEnumUtil {

    public static <E extends BaseCodeEnum> E codeOf(Class<E> enumClass, int code) {
        E[] enumConstants = enumClass.getEnumConstants();
        for (E e : enumConstants) {
            if (e.getCode() == code)
                return e;
        }
        return null;
    }
}
