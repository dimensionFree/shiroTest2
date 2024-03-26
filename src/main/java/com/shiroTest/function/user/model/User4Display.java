package com.shiroTest.function.user.model;

import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.shiroTest.function.base.BaseEntity;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class User4Display{

    private String id;


    private String role;

    private String state;

    private String username;

    private String email;


    public static User4Display User4Display(User user) {
        User4Display bean = JSONUtil.toBean(JSONUtil.toJsonStr(user), User4Display.class);
        return bean;
    }
}
