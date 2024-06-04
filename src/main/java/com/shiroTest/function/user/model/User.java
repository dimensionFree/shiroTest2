package com.shiroTest.function.user.model;

import com.shiroTest.function.base.BaseAuditableEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.shiroTest.function.quickMenu.MenuItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames= {"username"},name = "usernameUnique")},indexes = {})
@Entity
public class User extends BaseAuditableEntity {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    @Id
    private String id;

    @NotNull
    private String password;

    private String roleId;

    private String state;

    @NotBlank
    @Length(min = 4,max = 10,message = "username length error")
    private String username;

    @Email
    private String email;

    @ElementCollection(targetClass = MenuItem.class)
    @Enumerated(EnumType.ORDINAL) // 可以选择EnumType.ORDINAL以使用枚举的序数作为数据库中的存储方式
    @CollectionTable(name = "user_quick_menu_items", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name="quick_menu_item_ordinal")
    private Set<MenuItem> quickMenuItems= new HashSet<>();

    public User() {
    }

    public User(String username, String password,String roleId) {
        this.password = password;
        this.username = username;
    }
    public User(String username, String password) {
        this.password = password;
        this.username = username;
        this.roleId = roleId;

    }

    public void addMenuItem(MenuItem menuItem){
        this.quickMenuItems.add(menuItem);
    }




}
