package com.shiroTest.function.user.controller;


import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.common.ResultData;
import com.shiroTest.config.shiro.MyRealm;
import com.shiroTest.enums.ResultCodeEnum;
import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.UserPwdDto;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.BcryptUtil;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.shiroTest.function.base.BaseController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public UserController() {
        super(User.class);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserPwdDto userPwdDto) throws MyException {
        String username = userPwdDto.getUsername();
        String password = userPwdDto.getPassword();
        User byUsername = getService().getByUsername(username);
        if (Objects.nonNull(byUsername)){
            throw new MyException(ResultCodeEnum.USER_DUPLICATE,"用户已存在，无法注册");
        }
        User user = new User(username, BcryptUtil.encode(password), RoleServiceImpl.ROLE_ID_MEMBER);

        boolean save = getService().save(user);
        String jwtToken = getService().createTokenAndCache(user);
        return getService().getUserTokenResult(user,jwtToken);
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
        String jwtToken = getService().createTokenAndCache(existingUser);
        return getService().getUserTokenResult(existingUser, jwtToken);
    }

    @Override
    protected Result beforeReturn(Result success) {
        ResultData resultData = (ResultData) success.getBody();
        User dataContent = (User) resultData.getDataContent();
        resultData.setDataContent(getService().buildUser4Display(dataContent));
        return success;
    }


    @Override
    protected Result beforeReturnList(Result success) {
        ResultData resultData = (ResultData) success.getBody();
        var dataContent = (List<User>) resultData.getDataContent();
        resultData.setDataContent(dataContent.stream().map(u->getService().buildUser4Display(u)).collect(Collectors.toList()));
        return success;
    }
}

