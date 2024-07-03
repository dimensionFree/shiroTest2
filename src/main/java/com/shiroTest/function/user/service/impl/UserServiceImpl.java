package com.shiroTest.function.user.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.enums.ResultCodeEnum;
import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import com.shiroTest.function.user.dao.UserMapper;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.service.IUserService;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Objects;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author freedom
 * @since 2023-11-12
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RoleServiceImpl roleService;


    public User4Display buildUser4Display(User user) {
        User4Display bean = JSONUtil.toBean(JSONUtil.toJsonStr(user), User4Display.class);
        bean.setRole(roleService.getById(bean.getRoleId()));
        return bean;
    }

    public User getByUsername(String username) {
//        getBaseMapper().selectOne(new QueryWrapper<User>().eq("username",username))
        return getBaseMapper().selectByUsername(username);
    }

    public Result getUserTokenResult(User existingUser, String jwtToken) {
        return Result.success(UserLoginInfo.builder()
                .user4Display(buildUser4Display(existingUser))
                .token(jwtToken)
                .build());
    }

    public Result getUser4DisplayResult(User existingUser) {
        return Result.success(buildUser4Display(existingUser));
    }


    @Override
    @Transactional
    public boolean save(User entity) {
        boolean saveSuccess = this.retBool(getBaseMapper().insert(entity));
        if (saveSuccess && needCascade(entity)) {
            return retBool(getBaseMapper().cascadeInsert(entity));
        } else {
            return false;
        }
    }

    private boolean needCascade(User entity) {
        return !entity.getQuickMenuItems().isEmpty();
    }

    @Override
    public User getById(Serializable id) {
        User byId = super.getById(id);
        return byId;
    }

    public String createTokenAndCache(User existingUser) {
        String jwtToken = jwtUtil.createJwtToken(existingUser.getId(), 60 * 5);
        try {
            String key = redisUtil.buildUserTokenKey(jwtToken);
            redisUtil.set(key, buildUser4Display(existingUser));
        } catch (Exception e) {
            log.warn("redis error!", e);
        }
        return jwtToken;
    }

    public User getUserByToken(String token) throws MyException {
        User user = (User) redisUtil.get(redisUtil.buildUserTokenKey(token));
        if (Objects.isNull(user)) {
            String userId = jwtUtil.getUserId(token);
            return getById(userId);
        } else {
            throw new MyException(ResultCodeEnum.TOKEN_ERROR, "invalid token");
        }
    }
}
