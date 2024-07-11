package com.shiroTest.function.base;

import com.shiroTest.BackendApplication;
import com.shiroTest.utils.RedisUtil;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
//自动回滚数据库
@Transactional
public abstract class BaseTest extends TestCase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    RedisUtil redisUtil;

}
