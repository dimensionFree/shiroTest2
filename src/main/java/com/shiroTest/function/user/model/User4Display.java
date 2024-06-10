package com.shiroTest.function.user.model;

import cn.hutool.json.JSONUtil;
import com.shiroTest.function.quickMenu.MenuItem;
import com.shiroTest.function.role.model.Role;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class User4Display{

    private String id;


    private String roleId;

    private Role role;


    private String state;

    private String username;

    private String email;

    private Set<MenuItem> quickMenuItems= new HashSet<>();


}
