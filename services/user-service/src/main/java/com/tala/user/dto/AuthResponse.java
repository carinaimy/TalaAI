package com.tala.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String fullName;
    
    public static AuthResponse of(String token, Long userId, String email, String fullName) {
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(userId)
            .email(email)
            .fullName(fullName)
            .build();
    }
}
