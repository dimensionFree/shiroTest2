package com.shiroTest.function.role.controller;


import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.shiroTest.function.base.BaseController;
import com.shiroTest.function.role.model.Role;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author freedom
 * @since 2024-06-03
 */
@RestController
@RequestMapping("/api/role")
public class RoleController extends BaseController<Role, RoleServiceImpl> {

}

