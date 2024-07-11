package com.shiroTest.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.shiroTest.function.user.model.User;
import com.shiroTest.function.user.model.User4Display;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {


    private static final String DEFAULT_OPERATOR = "System";

    @Override
    public void insertFill(MetaObject metaObject) {

        String editorUserId;
        try {
            User4Display principal = null;
            principal = (User4Display) SecurityUtils.getSubject().getPrincipal();
            editorUserId = principal.getId();
        } catch (Exception e) {
            editorUserId=DEFAULT_OPERATOR;
            if (metaObject.getOriginalObject() instanceof User){
                var creatingUserId = ((User) metaObject.getOriginalObject()).getId();
                editorUserId=creatingUserId;
            }
            log.warn(e.getMessage());
        }
        this.setFieldValByName("createBy", editorUserId, metaObject);
        this.setFieldValByName("createDate", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updateBy", editorUserId, metaObject);
        this.setFieldValByName("updateDate", LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String id;
        try {
            User4Display principal = null;
            principal = (User4Display) SecurityUtils.getSubject().getPrincipal();
            id = principal.getId();
        } catch (Exception e) {
            id=DEFAULT_OPERATOR;
            log.warn(e.getMessage());
        }
        this.setFieldValByName("updateBy",id , metaObject);
        this.setFieldValByName("updateDate", LocalDateTime.now(), metaObject);
    }
}
