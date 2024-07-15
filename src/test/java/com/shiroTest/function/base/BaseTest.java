package com.shiroTest.function.base;

import com.shiroTest.BackendApplication;
import lombok.extern.slf4j.Slf4j;
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
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Rollback
@Slf4j
public abstract class BaseTest{

    protected static final ExecutorService executorService = Executors.newFixedThreadPool(2);
//
//    @Autowired
//    protected MockMvc mockMvc;

    public static Logger getLog() {
        return log;
    }

    protected void processDeleteTask(List<Runnable> tasks) {

        for (Runnable task : tasks) {
            if (task != null) {
                try {
                    // 在finally块中提交并执行任务
                    Future<?> submit = executorService.submit(task);
                    submit.get(); // 等待任务完成
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown();
                }
            }
        }
    }
}
