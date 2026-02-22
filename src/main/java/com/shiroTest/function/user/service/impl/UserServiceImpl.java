package com.shiroTest.function.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.common.MyException;
import com.shiroTest.common.Result;
import com.shiroTest.enums.ResultCodeEnum;
import com.shiroTest.function.mail.EmailService;
import com.shiroTest.function.role.service.impl.RoleServiceImpl;
import com.shiroTest.function.user.dao.UserMapper;
import com.shiroTest.function.user.model.State;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import com.shiroTest.function.user.model.UserLoginInfo;
import com.shiroTest.function.user.service.IUserService;
import com.shiroTest.utils.BcryptUtil;
import com.shiroTest.utils.JsonUtil;
import com.shiroTest.utils.JwtUtil;
import com.shiroTest.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;


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
    private static final long ACCESS_TOKEN_EXPIRE_SECONDS = 60L * 5L;
    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 60L * 60L * 24L * 30L;
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token_";

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    private EmailService emailService;


    public User4Display buildUser4Display(User user) {
        User4Display bean = JsonUtil.fromJson(JsonUtil.toJson(user), User4Display.class);
        bean.setRole(roleService.getById(bean.getRoleId()));
        return bean;
    }

    public User getByUsername(String username) {
//        getBaseMapper().selectOne(new QueryWrapper<User>().eq("username",username))
        return getBaseMapper().selectByUsername(username);
    }

    public UserLoginInfo getUserTokenResult(User existingUser, String jwtToken) {
        return getUserTokenResult(existingUser, jwtToken, "");
    }

    public UserLoginInfo getUserTokenResult(User existingUser, String jwtToken, String refreshToken) {
        User4Display user4Display = buildUser4Display(existingUser);
        UserLoginInfo build = UserLoginInfo.builder()
                .user4Display(user4Display)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .tokenExpireAt(jwtUtil.getExpireAtMillis(jwtToken))
                .refreshTokenExpireAt(jwtUtil.getExpireAtMillis(refreshToken))
                .build();
        return build;
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
            return saveSuccess;
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
        String jwtToken = jwtUtil.createJwtToken(existingUser.getId(), ACCESS_TOKEN_EXPIRE_SECONDS);
        try {
            String key = redisUtil.buildUserTokenKey(jwtToken);
            redisUtil.set(key, buildUser4Display(existingUser), ACCESS_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("redis error!", e);
        }
        return jwtToken;
    }

    public UserLoginInfo createLoginSession(User user) {
        String accessToken = createTokenAndCache(user);
        String refreshToken = createRefreshTokenAndCache(user.getId());
        return getUserTokenResult(user, accessToken, refreshToken);
    }

    private String createRefreshTokenAndCache(String userId) {
        HashMap<String, String> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tokenType", "refresh");
        String refreshToken = jwtUtil.createJwtToken(claims, REFRESH_TOKEN_EXPIRE_SECONDS);
        redisUtil.set(buildRefreshTokenKey(refreshToken), userId, REFRESH_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return refreshToken;
    }

    private String buildRefreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_KEY_PREFIX + refreshToken;
    }

    public User getUserByToken(String token) throws MyException {
        User user = (User) redisUtil.get(redisUtil.buildUserTokenKey(token));
        if (Objects.isNull(user)) {
            String userId = jwtUtil.getUserId(token);
            return getById(userId);
        } else {
            ResultCodeEnum error = ResultCodeEnum.TOKEN_ERROR;
            throw new MyException(error, error.getMessage());
        }
    }

    /**
     * auth based on role
     *
     * role -> admin member
     * auth-> admin -> {read_user,read_article,edit,}
     *
     *
     * @param username
     * @param password
     * @return
     * @throws MyException
     */

    public UserLoginInfo loginUser(String username, String password) throws MyException {
        User existingUser = getByUsername(username);
        if (Objects.isNull(existingUser)){
            ResultCodeEnum error = ResultCodeEnum.USER_NOT_EXISTS;
            throw new MyException(error, error.getMessage());
        }
        if (State.LOCKED.equals(existingUser.getState())){
            ResultCodeEnum error = ResultCodeEnum.USER_BLOCKED;
            throw new MyException(error, error.getMessage());
        }
        boolean match = BcryptUtil.match(password, existingUser.getPassword());
        if (!match){
            ResultCodeEnum error = ResultCodeEnum.USER_ERROR;
            throw new MyException(error, error.getMessage());
        }
        return createLoginSession(existingUser);
    }

    public UserLoginInfo refreshLoginSession(String refreshToken) throws MyException {
        if (Objects.isNull(refreshToken) || refreshToken.trim().isEmpty()) {
            ResultCodeEnum error = ResultCodeEnum.TOKEN_ERROR;
            throw new MyException(error, "refreshToken cannot be blank");
        }
        String normalizedRefreshToken = refreshToken.trim();
        String refreshKey = buildRefreshTokenKey(normalizedRefreshToken);

        if (!jwtUtil.verifyToken(normalizedRefreshToken) || jwtUtil.isExpire(normalizedRefreshToken)) {
            redisUtil.delete(refreshKey);
            ResultCodeEnum error = ResultCodeEnum.TOKEN_ERROR;
            throw new MyException(error, "refreshToken expired");
        }
        Object userIdObj = redisUtil.get(refreshKey);
        if (Objects.isNull(userIdObj)) {
            ResultCodeEnum error = ResultCodeEnum.TOKEN_ERROR;
            throw new MyException(error, "refreshToken invalid");
        }
        User user = getById(userIdObj.toString());
        if (Objects.isNull(user) || State.LOCKED.equals(user.getState())) {
            ResultCodeEnum error = ResultCodeEnum.USER_NOT_EXISTS;
            throw new MyException(error, "user not found");
        }
        redisUtil.delete(refreshKey);
        return createLoginSession(user);
    }

    public Result pingSession(String accessToken) throws MyException {
        if (Objects.isNull(accessToken) || accessToken.trim().isEmpty()) {
            ResultCodeEnum error = ResultCodeEnum.TOKEN_ERROR;
            throw new MyException(error, "token cannot be blank");
        }
        String token = accessToken.trim();
        if (!jwtUtil.verifyToken(token) || jwtUtil.isExpire(token)) {
            redisUtil.delete(redisUtil.buildUserTokenKey(token));
            ResultCodeEnum error = ResultCodeEnum.TOKEN_ERROR;
            throw new MyException(error, "token expired");
        }
        String tokenKey = redisUtil.buildUserTokenKey(token);
        Long ttl = redisUtil.getExpire(tokenKey, TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            long remainMillis = jwtUtil.getExpireAtMillis(token) - System.currentTimeMillis();
            long remainSeconds = Math.max(1L, remainMillis / 1000L);
            redisUtil.expire(tokenKey, remainSeconds, TimeUnit.SECONDS);
            ttl = remainSeconds;
        }
        return Result.success(java.util.Map.of(
                "authenticated", true,
                "tokenTtlSeconds", ttl
        ));
    }

    public User getByEmail(String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email",email);
        return getBaseMapper().selectOne(queryWrapper);
    }

    public String generateVerificationCode() {
        // 生成6位随机数字验证码
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void saveVerificationCode(String email, String code) {
        // 使用 Redis 存储验证码，设置过期时间为10分钟
        redisUtil.set(getVerificationCacheKey(email), code, 10, TimeUnit.MINUTES);
    }

    private String getVerificationCacheKey(String email) {
        return "email_verification_code:" + email;
    }

    public void sendVerificationEmail(String email, String code) {
        String message = "あなたの認証コードは：" + code + "，期限は10分です。";
        emailService.sendEmail(email,"メール認証",message);

    }


    public void checkVerificationCode(String email, String verificationCode) throws MyException {
        if (!verificationCode.equals(redisUtil.get(getVerificationCacheKey(email)))) {
            ResultCodeEnum error = ResultCodeEnum.VERIFICATION_NOT_MATCH;
            throw new MyException(error, error.getMessage());
        }
    }
    public void checkEmailRegistering(String email) throws MyException {

        if (Objects.nonNull(redisUtil.get(getVerificationCacheKey(email)))) {
            ResultCodeEnum error = ResultCodeEnum.VERIFICATION_CODE_EXIST;
            throw new MyException(error, error.getMessage());
        }
    }
}
