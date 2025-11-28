package com.tala.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * File Service Application
 * Handles file upload, storage, and management
 */
@SpringBootApplication(scanBasePackages = {"com.tala.file", "com.tala.core"})
@EnableJpaRepositories(basePackages = "com.tala.file.repository")
public class FileServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }
}
