package com.shiroTest.function.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.Result;
import com.shiroTest.function.user.dao.UserMapper;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.service.IUserService;
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

    public static Result getUserTokenResult(User existingUser, String jwtToken) {
        return Result.success(UserLoginInfo.builder()
                .user4Display(User4Display.User4Display(existingUser))
                .token(jwtToken)
                .build());
    }
    public static Result getUser4DisplayResult(User existingUser) {
        return Result.success(User4Display.User4Display(existingUser));
    }

    @Override
    public boolean save(User entity) {
//        getBaseMapper().insert(entity);
        //add cascade save
        return super.save(entity);
    }
}
