package com.tala.reminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Reminder Service Application
 * 
 * Manages reminders and heads-up notifications
 */
@SpringBootApplication(scanBasePackages = {"com.tala.reminder", "com.tala.core"})
@EntityScan(basePackages = {"com.tala.reminder.domain", "com.tala.core.domain"})
@EnableJpaRepositories(basePackages = "com.tala.reminder.repository")
@EnableKafka
public class ReminderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReminderServiceApplication.class, args);
    }
}
