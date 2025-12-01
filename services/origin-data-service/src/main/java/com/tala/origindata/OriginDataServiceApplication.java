package com.tala.origindata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Origin Data Service Application
 * 
 * Stores all original data sources for TalaAI:
 * - DayCare Reports
 * - Incident Reports
 * - Health Reports
 * - Home Events
 * 
 * Uses event sourcing pattern with original + AI-extracted data separation
 */
@SpringBootApplication(scanBasePackages = {"com.tala.origindata", "com.tala.core"})
@EnableJpaAuditing
@EnableFeignClients
public class OriginDataServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OriginDataServiceApplication.class, args);
    }
}
