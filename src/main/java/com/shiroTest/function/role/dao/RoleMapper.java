package com.shiroTest.function.role.dao;

import com.shiroTest.function.role.model.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author freedom
 * @since 2024-06-03
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Override
    Role selectById(Serializable id);
}
