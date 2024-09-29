package com.shiroTest.function.user.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.shiroTest.function.base.BaseAuditableEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.shiroTest.function.quickMenu.MenuItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 
 * </p>
 *
 * @author freedom
 * @since 2023-11-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

public class User extends BaseAuditableEntity {

    private static final long serialVersionUID=1L;

    @NotNull
    private String password;

    private String roleId;

    private String state= State.ACTIVE.name();

    @NotBlank
    @Length(min = 4,max = 10,message = "username length error")
    private String username;

    @Email
    private String email;

    @TableField(exist = false)
    private Set<MenuItem> quickMenuItems= new HashSet<>();

    public User() {
    }

    public User(String username, String password,String roleId) {
        this.password = password;
        this.username = username;
        this.roleId = roleId;

    }
    public User(String username, String password,String email,String roleId) {
        this.password = password;
        this.username = username;
        this.email = email;
        this.roleId = roleId;

    }
    public User(String username, String password) {
        this.password = password;
        this.username = username;

    }

    public void addMenuItem(MenuItem menuItem){
        this.quickMenuItems.add(menuItem);
    }




}
