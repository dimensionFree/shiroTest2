package com.shiroTest.function.base;

import com.shiroTest.BackendApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@AutoConfigureMockMvc(addFilters = false)
@EnableWebMvc
@WebAppConfiguration
//@RunWith(SpringJUnit4ClassRunner.class)(todo:what the diff with SpringRunner.class?)
//@WebMvcTest(UserController.class)
public abstract class BaseControllerTest extends BaseTest {
}
