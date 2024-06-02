package com.shiroTest.function.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.function.role.model.Role;
import com.shiroTest.function.role.dao.RoleMapper;
import com.shiroTest.function.role.service.IRoleService;
import org.springframework.stereotype.Service;

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

}
