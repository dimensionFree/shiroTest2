package com.shiroTest.function.base;

import com.shiroTest.BackendApplication;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BackendApplication.class)
//自动回滚数据库
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest{
//
//    @Autowired
//    protected MockMvc mockMvc;

}
