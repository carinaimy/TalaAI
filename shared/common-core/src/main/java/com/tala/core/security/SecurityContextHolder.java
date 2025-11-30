package com.tala.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

/**
 * Utility class to access authenticated user information from security context
 */
public final class SecurityContextHolder {
    
    private SecurityContextHolder() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Get current authenticated user ID
     * @return User ID or null if not authenticated
     */
    public static Long getCurrentUserId() {
        SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        
        return null;
    }
    
    /**
     * Check if user is authenticated
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * Get current authentication object
     * @return Authentication or null if not authenticated
     */
    public static Authentication getAuthentication() {
        SecurityContext context = org.springframework.security.core.context.SecurityContextHolder.getContext();
        return context.getAuthentication();
    }
}
