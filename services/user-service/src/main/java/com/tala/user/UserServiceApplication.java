package com.tala.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Service Application
 * 
 * User authentication and profile management
 */
@SpringBootApplication(scanBasePackages = {"com.tala.user", "com.tala.core"})
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"com.tala.user.domain", "com.tala.core.domain"})
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.tala.user.repository")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
