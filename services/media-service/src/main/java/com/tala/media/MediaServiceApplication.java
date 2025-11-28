package com.tala.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Media Service Application
 * 
 * Manages media items and photo gallery
 */
@SpringBootApplication(scanBasePackages = {"com.tala.media", "com.tala.core"})
@EntityScan(basePackages = {"com.tala.media.domain", "com.tala.core.domain"})
@EnableJpaRepositories(basePackages = "com.tala.media.repository")
public class MediaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }
}
