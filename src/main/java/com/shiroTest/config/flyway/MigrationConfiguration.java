package com.shiroTest.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

/**
 * make sure flyway run after jpa ddl
 */
@Configuration
public class MigrationConfiguration {

    /**
     * Override default flyway initializer to do nothing
     */
    @Bean
    @Lazy
    FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, (f) ->{} );
    }
//   commented cause loop dependence
//    /**
//     * Create a second flyway initializer to run after jpa has created the schema
//     */
//    @Bean
//    @DependsOn("entityManagerFactory")
//    FlywayMigrationInitializer delayedFlywayInitializer(Flyway flyway) {
//        return new FlywayMigrationInitializer(flyway, null);
//    }

}


