package com.shiroTest.function.base;

import com.shiroTest.BackendApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = BackendApplication.class)
//自动回滚数据库
//@Transactional
//@Rollback
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class BaseTest{

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    protected void clearMybatisLvl1Cache(){
        SqlSession sqlSession = sqlSessionFactory.openSession();
        sqlSession.clearCache();
    }

//
//    @Autowired
//    protected MockMvc mockMvc;

    public static Logger getLog() {
        return log;
    }

}
