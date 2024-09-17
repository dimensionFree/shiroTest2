package com.shiroTest.function.base;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MyServiceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDataSource() throws Exception {
        System.out.println("DataSource URL: " + dataSource.getConnection().getMetaData().getURL());
    }
}
