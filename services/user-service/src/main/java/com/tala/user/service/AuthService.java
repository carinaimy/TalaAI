package com.tala.user.service;

import com.tala.core.exception.ErrorCode;
import com.tala.core.exception.TalaException;
import com.tala.user.domain.User;
import com.tala.user.dto.AuthResponse;
import com.tala.user.dto.LoginRequest;
import com.tala.user.dto.ProfileResponse;
import com.tala.user.dto.RegisterRequest;
import com.tala.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Authentication service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ProfileService profileService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new TalaException(ErrorCode.USER_ALREADY_EXISTS, 
                "User with email " + request.getEmail() + " already exists");
        }
        
        // Create user with BCrypt hashed password
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName())
            .build();
        
        user = userRepository.save(user);
        
        // Generate access and refresh tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        Long expiresIn = jwtService.getAccessTokenExpirationMs() / 1000; // Convert to seconds
        
        // Fetch user's baby profiles
        List<ProfileResponse> babyProfiles = profileService.getUserProfiles(user.getId());
        
        log.info("User registered successfully: {}, profiles count: {}", user.getEmail(), babyProfiles.size());
        
        return AuthResponse.of(accessToken, refreshToken, expiresIn, user.getId(), user.getEmail(), user.getFullName(), babyProfiles);
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());
        
        // Find user
        User user = userRepository.findByEmailAndNotDeleted(request.getEmail())
            .orElseThrow(() -> new TalaException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid email or password"));
        
        // Verify password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new TalaException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid email or password");
        }
        
        // Generate access and refresh tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        Long expiresIn = jwtService.getAccessTokenExpirationMs() / 1000; // Convert to seconds
        
        // Fetch user's baby profiles
        List<ProfileResponse> babyProfiles = profileService.getUserProfiles(user.getId());
        
        log.info("User logged in successfully: {}, profiles count: {}", user.getEmail(), babyProfiles.size());
        
        return AuthResponse.of(accessToken, refreshToken, expiresIn, user.getId(), user.getEmail(), user.getFullName(), babyProfiles);
    }
    
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refresh token request");
        
        // Validate refresh token
        if (!jwtService.validateToken(refreshToken)) {
            throw new TalaException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid or expired refresh token");
        }
        
        // Verify it's a refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new TalaException(ErrorCode.INVALID_CREDENTIALS, 
                "Token is not a refresh token");
        }
        
        // Extract user information
        Long userId = jwtService.getUserIdFromToken(refreshToken);
        String email = jwtService.getEmailFromToken(refreshToken);
        
        if (userId == null || email == null) {
            throw new TalaException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid token claims");
        }
        
        // Verify user still exists and is not deleted
        User user = userRepository.findByIdAndNotDeleted(userId)
            .orElseThrow(() -> new TalaException(ErrorCode.USER_NOT_FOUND, 
                "User not found or has been deleted"));
        
        // Generate new access and refresh tokens
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        Long expiresIn = jwtService.getAccessTokenExpirationMs() / 1000;
        
        log.info("Tokens refreshed successfully for user: {}", user.getEmail());
        
        return AuthResponse.of(newAccessToken, newRefreshToken, expiresIn, user.getId(), user.getEmail(), user.getFullName());
    }
}
