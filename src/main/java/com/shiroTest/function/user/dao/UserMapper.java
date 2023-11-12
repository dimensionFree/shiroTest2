package com.shiroTest.function.user.dao;

import com.shiroTest.function.user.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author freedom
 * @since 2023-11-12
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    public User getByUsername(String username);
}
