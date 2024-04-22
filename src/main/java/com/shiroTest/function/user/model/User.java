package com.shiroTest.function.user.model;

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
@Table
@Entity
public class User extends BaseAuditableEntity {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    @Id
    private String id;

    @NotNull
    private String password;

    private String role;

    private String state;

    @NotBlank
    @Length(min = 4,max = 10,message = "username length error")
    private String username;

    @Email
    private String email;

    @ElementCollection(targetClass = MenuItem.class)
    @Enumerated(EnumType.ORDINAL) // 可以选择EnumType.ORDINAL以使用枚举的序数作为数据库中的存储方式
    @CollectionTable(name = "user_quick_menu_items", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name="quick_menu_items")
    private Set<MenuItem> quickMenuItems=Set.of(MenuItem.PART_A,MenuItem.PART_B,MenuItem.PART_C);

    public User() {
    }

    public User(String username, String password) {
        this.password = password;
        this.username = username;
    }
}
