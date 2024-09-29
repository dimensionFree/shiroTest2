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


public class Role extends BaseAuditableEntity {

    private static final long serialVersionUID=1L;

    private String roleName;

    private Set<Authority> authorities = new HashSet<>();


    private Set<Feature> features = new HashSet<>();

    public Set<Authority> getAuthorities() {
        if (authorities.contains(Authority.ALL)){
            return Arrays.stream(Authority.values()).collect(Collectors.toSet());
        }
        return authorities;
    }
}
