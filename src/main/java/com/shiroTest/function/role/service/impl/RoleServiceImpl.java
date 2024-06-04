package com.shiroTest.function.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.function.role.model.Authority;
import com.shiroTest.function.role.model.Role;
import com.shiroTest.function.role.dao.RoleMapper;
import com.shiroTest.function.role.service.IRoleService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author freedom
 * @since 2024-06-03
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {
    public static final String ROLE_ID_ADMIN = "id_admin";
    public static final String ROLE_ID_MEMBER = "id_member";


    public List<String> getRolePermissions(String roleId) {
        Role role = getById(roleId);

        Set<Authority> authorities = role.getAuthorities();
        if (authorities.contains(Authority.ALL)){
            return Arrays.stream(Authority.values()).map(i->i.name()).collect(Collectors.toList());
        }
        return authorities.stream().map(i -> i.name()).collect(Collectors.toList());
    }
}
