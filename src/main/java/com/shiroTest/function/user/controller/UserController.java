package com.shiroTest.function.user.controller;


import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.common.ResultData;
import com.shiroTest.enums.ResultCodeEnum;
import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.model.UserPwdDto;
import com.shiroTest.function.user.service.impl.UserServiceImpl;
import com.shiroTest.utils.BcryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.shiroTest.function.base.BaseController;

import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/user")
@Slf4j
public class UserController extends BaseController<User, UserServiceImpl> {

    public UserController() {
        super(User.class);
    }

    @PostMapping("/register")
    public Result register(@Validated(UserPwdDto.Register.class) @RequestBody UserPwdDto userPwdDto) throws MyException {
        String username = userPwdDto.getUsername();
        String password = userPwdDto.getPassword();
        String email = userPwdDto.getEmail();
        String verificationCode = userPwdDto.getVerificationCode();

        getService().checkVerificationCode(email,verificationCode);

        User byUsername = getService().getByUsername(username);
        if (Objects.nonNull(byUsername)){
            ResultCodeEnum error = ResultCodeEnum.USER_DUPLICATE;
            throw new MyException(error, error.getMessage());
        }
        User user = new User(username, BcryptUtil.encode(password),email, RoleServiceImpl.ROLE_ID_MEMBER);


        boolean save = getService().save(user);
        String jwtToken = getService().createTokenAndCache(user);
        return Result.success(
                getService().getUserTokenResult(user,jwtToken)
        );
    }

    @PostMapping("/login")
    public Result login(@Validated(UserPwdDto.Login.class) @RequestBody UserPwdDto userPwdDto) throws MyException {
        String username = userPwdDto.getUsername();
        String password = userPwdDto.getPassword();

        return Result.success(
                getService().loginUser(username,password)
        );
    }

    @Override
    protected Object beforeReturn(User success) {
        if (Objects.isNull(success)){
            return null;
        }
        User4Display user4Display = getService().buildUser4Display(success);
        return user4Display;
    }

    @Override
    protected List beforeReturnList(List<User> datas) {
        return datas.stream().map(u->beforeReturn(u)).collect(Collectors.toList());
    }


    @PostMapping("/send-verification-code")
    public Result sendVerificationCode(@RequestBody Map<String, String> request) throws MyException {
        String email = request.get("email");
        // 检查邮箱是否已被注册
        User existingUser = getService().getByEmail(email);
        if (existingUser != null) {
            ResultCodeEnum error = ResultCodeEnum.EMAIL_ALREADY_REGISTER;
            throw new MyException(error, error.getMessage());
        }

        getService().checkEmailRegistering(email);

        // 生成验证码
        String verificationCode = getService().generateVerificationCode();

        // 将验证码和邮箱关联，并存储在临时存储（例如Redis），设置有效期
        getService().saveVerificationCode(email, verificationCode);

        // 发送验证码到邮箱
        getService().sendVerificationEmail(email, verificationCode);

        return Result.success("認証コード発送済み、メールをチェックしてください");
    }


}

