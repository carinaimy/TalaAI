package com.tala.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String email;
    private String fullName;
    private List<ProfileResponse> babyProfiles;
    
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, Long userId, String email, String fullName) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .userId(userId)
            .email(email)
            .fullName(fullName)
            .build();
    }
    
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, Long userId, String email, String fullName, List<ProfileResponse> babyProfiles) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .userId(userId)
            .email(email)
            .fullName(fullName)
            .babyProfiles(babyProfiles)
            .build();
    }
}
