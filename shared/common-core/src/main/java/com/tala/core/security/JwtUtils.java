package com.tala.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Utility class for token generation and validation
 */
@Slf4j
public final class JwtUtils {
    
    private JwtUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Generate access token
     */
    public static String generateToken(Long userId, String email, String secret) {
        return generateToken(userId, email, secret, JwtConstants.ACCESS_TOKEN_EXPIRATION_MS, JwtConstants.TOKEN_TYPE_ACCESS);
    }
    
    /**
     * Generate refresh token
     */
    public static String generateRefreshToken(Long userId, String email, String secret) {
        return generateToken(userId, email, secret, JwtConstants.REFRESH_TOKEN_EXPIRATION_MS, JwtConstants.TOKEN_TYPE_REFRESH);
    }
    
    /**
     * Generate JWT token with custom expiration and type
     */
    private static String generateToken(Long userId, String email, String secret, long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        SecretKey key = getSigningKey(secret);
        
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(JwtConstants.CLAIM_USER_ID, userId)
                .claim(JwtConstants.CLAIM_EMAIL, email)
                .claim(JwtConstants.CLAIM_TOKEN_TYPE, tokenType)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validate JWT token
     */
    public static boolean validateToken(String token, String secret) {
        try {
            SecretKey key = getSigningKey(secret);
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract user ID from token
     */
    public static Long getUserIdFromToken(String token, String secret) {
        Claims claims = getClaimsFromToken(token, secret);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get(JwtConstants.CLAIM_USER_ID);
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }
    
    /**
     * Extract email from token
     */
    public static String getEmailFromToken(String token, String secret) {
        Claims claims = getClaimsFromToken(token, secret);
        return claims != null ? claims.get(JwtConstants.CLAIM_EMAIL, String.class) : null;
    }
    
    /**
     * Extract all claims from token
     */
    public static Claims getClaimsFromToken(String token, String secret) {
        try {
            SecretKey key = getSigningKey(secret);
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract claims from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if token is expired
     */
    public static boolean isTokenExpired(String token, String secret) {
        try {
            Claims claims = getClaimsFromToken(token, secret);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * Extract token from Authorization header
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(JwtConstants.BEARER_PREFIX)) {
            return authHeader.substring(JwtConstants.BEARER_PREFIX.length());
        }
        return null;
    }
    
    /**
     * Get token type from token
     */
    public static String getTokenType(String token, String secret) {
        Claims claims = getClaimsFromToken(token, secret);
        return claims != null ? claims.get(JwtConstants.CLAIM_TOKEN_TYPE, String.class) : null;
    }
    
    /**
     * Validate that token is an access token
     */
    public static boolean isAccessToken(String token, String secret) {
        String tokenType = getTokenType(token, secret);
        return JwtConstants.TOKEN_TYPE_ACCESS.equals(tokenType);
    }
    
    /**
     * Validate that token is a refresh token
     */
    public static boolean isRefreshToken(String token, String secret) {
        String tokenType = getTokenType(token, secret);
        return JwtConstants.TOKEN_TYPE_REFRESH.equals(tokenType);
    }
    
    /**
     * Get signing key from secret
     */
    private static SecretKey getSigningKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
