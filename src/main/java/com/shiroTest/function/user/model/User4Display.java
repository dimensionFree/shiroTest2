package com.shiroTest.function.user.model;

import cn.hutool.json.JSONUtil;
import com.shiroTest.function.quickMenu.MenuItem;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class User4Display{

    private String id;


    private String roleId;

    private String state;

    private String username;

    private String email;

    private Set<MenuItem> quickMenuItems= new HashSet<>();


    public static User4Display User4Display(User user) {
        User4Display bean = JSONUtil.toBean(JSONUtil.toJsonStr(user), User4Display.class);
        return bean;
    }
}
