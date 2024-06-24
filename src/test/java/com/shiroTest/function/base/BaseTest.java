package com.shiroTest.function.base;

import com.shiroTest.BackendApplication;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
//自动回滚数据库
@Transactional
public abstract class BaseTest extends TestCase {
}
