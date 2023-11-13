package com.shiroTest.function.user.controller;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.config.shiro.MyRealm;
import com.shiroTest.enums.ResultCodeEnum;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.BcryptUtil;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.shiroTest.function.base.BaseController;

import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import java.util.Map;
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
public class UserController extends BaseController<User, UserServiceImpl> {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    RedisUtil redisUtil;

    @PostMapping("/register")
    public Result register(@RequestParam @NotBlank String username,@RequestParam @NotBlank String password) throws MyException {
        User byUsername = getService().getByUsername(username);
        if (Objects.nonNull(byUsername)){
            throw new MyException(ResultCodeEnum.USER_DUPLICATE,"用户已存在，无法注册");
        }
        User user = new User(username, BcryptUtil.encode(password));
        boolean save = getService().save(user);
        String jwtToken = jwtUtil.createJwtToken(user.getId().toString(), 60*5);
        return Result.success(jwtToken);
    }

    @PostMapping("/login")
    public Result login(@NotBlank @RequestParam String username, @NotBlank  String password) throws MyException {
        User byUsername = getService().getByUsername(username);
        if (Objects.isNull(byUsername)){
            throw new MyException(ResultCodeEnum.USER_NOT_EXISTS,"用户不存在，无法登录");
        }
        boolean match = BcryptUtil.match(password, byUsername.getPassword());
        if (!match){
            throw new MyException(ResultCodeEnum.USER_ERROR,"wrong pwd，无法登录");
        }
        String jwtToken = jwtUtil.createJwtToken(byUsername.getId(), 60 * 5);
        redisUtil.set(MyRealm.USER_KEY_PREFIX+jwtToken,byUsername);
        return Result.success(jwtToken);
    }

}

