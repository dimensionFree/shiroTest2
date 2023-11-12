package com.shiroTest.function.user.model;

import com.shiroTest.function.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
@Table
@Entity
public class User extends BaseEntity {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    @Id
    private String id;

    @NotNull
    @Length(min = 4,max = 10,message = "pwd length error")
    private String password;

    private String role;

    private String state;
    @NotBlank
    @Length(min = 4,max = 10,message = "username length error")
    private String username;

    @Email
    private String email;


    public User() {
    }

    public User(String username, String password) {
        this.password = password;
        this.username = username;
    }
}
