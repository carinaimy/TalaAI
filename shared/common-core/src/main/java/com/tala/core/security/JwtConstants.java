package com.tala.core.security;

/**
 * JWT Security Constants
 */
public final class JwtConstants {
    
    private JwtConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // Header names
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    
    // JWT Claims
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    
    // Token types
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    
    // Token expiration
    public static final long ACCESS_TOKEN_EXPIRATION_MS = 3600000L; // 1 hour
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 604800000L; // 7 days
}
