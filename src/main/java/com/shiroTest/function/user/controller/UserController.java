package com.shiroTest.function.user.controller;


import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.config.shiro.MyRealm;
import com.shiroTest.enums.ResultCodeEnum;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.model.UserPwdDto;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.BcryptUtil;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.shiroTest.function.base.BaseController;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author freedom
 * @since 2023-11-12
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController extends BaseController<User, UserServiceImpl> {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    RedisUtil redisUtil;

    @PostMapping("/register")

    public Result register(@RequestBody UserPwdDto userPwdDto) throws MyException {
        String username = userPwdDto.getUsername();
        String password = userPwdDto.getPassword();
        User byUsername = getService().getByUsername(username);
        if (Objects.nonNull(byUsername)){
            throw new MyException(ResultCodeEnum.USER_DUPLICATE,"用户已存在，无法注册");
        }
        User user = new User(username, BcryptUtil.encode(password));
        boolean save = getService().save(user);
        String jwtToken = createTokenAndCache(user);
        return getUserTokenResult(user,jwtToken);
    }

    @PostMapping("/login")
    public Result login(@RequestBody UserPwdDto userPwdDto) throws MyException {
        String username = userPwdDto.getUsername();
        String password = userPwdDto.getPassword();
        User existingUser = getService().getByUsername(username);
        if (Objects.isNull(existingUser)){
            throw new MyException(ResultCodeEnum.USER_NOT_EXISTS,"用户不存在，无法登录");
        }
        boolean match = BcryptUtil.match(password, existingUser.getPassword());
        if (!match){
            throw new MyException(ResultCodeEnum.USER_ERROR,"wrong pwd，无法登录");
        }
        String jwtToken = createTokenAndCache(existingUser);
        return getUserTokenResult(existingUser, jwtToken);
    }

    private String createTokenAndCache(User existingUser) {
        String jwtToken = jwtUtil.createJwtToken(existingUser.getId(), 60 * 5);
        try {
            String key = MyRealm.USER_KEY_PREFIX + jwtToken;
            redisUtil.set(key, existingUser);
        } catch (Exception e) {
            log.warn("redis error!",e);
        }
        return jwtToken;
    }

    private Result getUserTokenResult(User existingUser, String jwtToken) {
        return Result.success(UserLoginInfo.builder()
                .user4Display(User4Display.User4Display(existingUser))
                .token(jwtToken)
                .build());
    }

}

