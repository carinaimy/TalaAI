package com.tala.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Event Service Application
 * 
 * Manages baby care events with Kafka integration
 */
@SpringBootApplication(scanBasePackages = {"com.tala.event", "com.tala.core"})
@EntityScan(basePackages = {"com.tala.event.domain", "com.tala.core.domain"})
@EnableJpaRepositories(basePackages = "com.tala.event.repository")
@EnableKafka
@EnableCaching
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }
}
