package com.shiroTest.function.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.dao.UserMapper;
import com.shiroTest.function.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author freedom
 * @since 2023-11-12
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    public User getByUsername(String username){
//        getBaseMapper().selectOne(new QueryWrapper<User>().eq("username",username))
        return getBaseMapper().getByUsername(username);
    }
}
