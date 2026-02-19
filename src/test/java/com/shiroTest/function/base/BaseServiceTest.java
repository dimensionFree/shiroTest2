package com.shiroTest.function.base;

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

//@RunWith(SpringRunner.class)
@Transactional
@Rollback
public abstract class BaseServiceTest extends BaseTest {
}
