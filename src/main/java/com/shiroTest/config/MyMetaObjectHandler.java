package com.shiroTest.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    private static final String DEFAULT_OPERATOR = "System";

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createBy", DEFAULT_OPERATOR, metaObject);
        this.setFieldValByName("createDate", new Date(), metaObject);
        this.setFieldValByName("updateBy", DEFAULT_OPERATOR, metaObject);
        this.setFieldValByName("updateDate", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateBy", DEFAULT_OPERATOR, metaObject);
        this.setFieldValByName("updateDate", new Date(), metaObject);
    }
}
