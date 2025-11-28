package com.tala.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;

/**
 * JWT token service (simplified version)
 */
@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret:dev-secret-key-change-in-production-minimum-64-characters-long}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    public String generateToken(Long userId, String email) {
        // Simplified JWT generation
        // In production, use a proper JWT library like jjwt
        long expirationTime = Instant.now().toEpochMilli() + jwtExpiration;
        String payload = userId + ":" + email + ":" + expirationTime;
        String encoded = Base64.getEncoder().encodeToString(payload.getBytes());
        
        log.debug("Generated token for user: {}", email);
        
        return encoded;
    }
    
    public Long getUserIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            log.error("Invalid token", e);
            return null;
        }
    }
}
