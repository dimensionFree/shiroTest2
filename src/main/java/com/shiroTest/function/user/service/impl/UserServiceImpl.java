package com.shiroTest.function.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.config.shiro.MyRealm;
import com.shiroTest.enums.ResultCodeEnum;
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
 *  服务实现类
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

    public User getByUsername(String username){
//        getBaseMapper().selectOne(new QueryWrapper<User>().eq("username",username))
        return getBaseMapper().selectByUsername(username);
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
    @Transactional
    public boolean save(User entity) {
        boolean saveSuccess = this.retBool(getBaseMapper().insert(entity));
        if (saveSuccess&&needCascade(entity)){
             return retBool(getBaseMapper().cascadeInsert(entity));
        }else {
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
            String key = getUserCacheKey(jwtToken);
            redisUtil.set(key, existingUser);
        } catch (Exception e) {
            log.warn("redis error!",e);
        }
        return jwtToken;
    }

    private String getUserCacheKey(String jwtToken) {
        return MyRealm.USER_KEY_PREFIX + jwtToken;
    }

    public User getUserByToken(String token) throws MyException {
        User user = (User) redisUtil.get(getUserCacheKey(token));
        if (Objects.isNull(user)){
            String userId = jwtUtil.getUserId(token);
            return getById(userId);
        }else {
            throw new MyException(ResultCodeEnum.TOKEN_ERROR,"invalid token");
        }
    }
}
