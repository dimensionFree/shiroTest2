package com.shiroTest.function.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiroTest.function.user.dao.UserMapper;
import com.shiroTest.function.user.model.User;

public class BaseService<M extends BaseMapper<T>,T extends BaseEntity> extends ServiceImpl<M, T> {
}
