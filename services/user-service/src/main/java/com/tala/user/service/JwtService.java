package com.tala.user.service;

import com.tala.core.security.JwtConstants;
import com.tala.core.security.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT token service using proper jjwt library
 */
@Service
@Slf4j
public class JwtService {
    
    @Value("${jwt.secret:dev-secret-key-change-in-production-minimum-64-characters-long}")
    private String jwtSecret;
    
    public String generateAccessToken(Long userId, String email) {
        String token = JwtUtils.generateToken(userId, email, jwtSecret);
        log.debug("Generated access token for user: {}", email);
        return token;
    }
    
    public String generateRefreshToken(Long userId, String email) {
        String token = JwtUtils.generateRefreshToken(userId, email, jwtSecret);
        log.debug("Generated refresh token for user: {}", email);
        return token;
    }
    
    public Long getAccessTokenExpirationMs() {
        return JwtConstants.ACCESS_TOKEN_EXPIRATION_MS;
    }
    
    public boolean validateToken(String token) {
        return JwtUtils.validateToken(token, jwtSecret);
    }
    
    public Long getUserIdFromToken(String token) {
        return JwtUtils.getUserIdFromToken(token, jwtSecret);
    }
    
    public String getEmailFromToken(String token) {
        return JwtUtils.getEmailFromToken(token, jwtSecret);
    }
    
    public Claims getClaimsFromToken(String token) {
        return JwtUtils.getClaimsFromToken(token, jwtSecret);
    }
    
    public boolean isTokenExpired(String token) {
        return JwtUtils.isTokenExpired(token, jwtSecret);
    }
    
    public boolean isAccessToken(String token) {
        return JwtUtils.isAccessToken(token, jwtSecret);
    }
    
    public boolean isRefreshToken(String token) {
        return JwtUtils.isRefreshToken(token, jwtSecret);
    }
}
