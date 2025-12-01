package com.tala.origindata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Health Check Controller
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    
    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Origin Data Service is running");
    }
}
