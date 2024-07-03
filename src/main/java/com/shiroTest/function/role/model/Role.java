package com.shiroTest.function.role.model;

import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.base.BaseAuditableEntity;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;

/**
 * <p>
 * 
 * </p>
 *
 * @author freedom
 * @since 2024-06-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Table
@Entity
public class Role extends BaseAuditableEntity {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    @Id
    private String id;

    private String roleName;

    @ElementCollection(targetClass = Authority.class)
    @Enumerated(EnumType.ORDINAL) // 可以选择EnumType.ORDINAL以使用枚举的序数作为数据库中的存储方式
    @CollectionTable(name = "role_authorities", joinColumns = @JoinColumn(name = "role_id"))
    private Set<Authority> authorities = new HashSet<>();


    @ElementCollection(targetClass = Feature.class)
    @Enumerated(EnumType.ORDINAL) // 可以选择EnumType.ORDINAL以使用枚举的序数作为数据库中的存储方式
    @CollectionTable(name = "role_features", joinColumns = @JoinColumn(name = "role_id"))
    private Set<Feature> features = new HashSet<>();

    public Set<Authority> getAuthorities() {
        if (authorities.contains(Authority.ALL)){
            return Arrays.stream(Authority.values()).collect(Collectors.toSet());
        }
        return authorities;
    }
}
