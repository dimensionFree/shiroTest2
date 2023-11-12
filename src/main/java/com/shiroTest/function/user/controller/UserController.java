package com.shiroTest.function.user.controller;


import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.enums.ResultCodeEnum;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.BcryptUtil;
import com.shiroTest.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/register")
    @Transactional(rollbackOn =  Exception.class)
    public Result register(@RequestBody Map<String,String> request) throws MyException {
        String username = request.get("username");
        String password = request.get("password");
        User byUsername = getService().getByUsername(username);
        if (Objects.nonNull(byUsername)){
            throw new MyException(ResultCodeEnum.USER_DUPLICATE,"用户已存在，无法注册");
        }
        User user = new User(username, BcryptUtil.encode(password));
        boolean save = getService().save(user);
        //didnt rollback?
        String jwtToken = jwtUtil.createJwtToken(user.getId().toString(), 10);
        return Result.success(jwtToken);
    }

    @PostMapping("/login")
    public String login(@NotBlank String userName,@NotBlank  String pwd){
        return "";
    }

}

