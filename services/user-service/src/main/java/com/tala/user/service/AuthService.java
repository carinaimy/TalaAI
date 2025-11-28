package com.tala.user.service;

import com.tala.core.exception.ErrorCode;
import com.tala.core.exception.TalaException;
import com.tala.user.domain.User;
import com.tala.user.dto.AuthResponse;
import com.tala.user.dto.LoginRequest;
import com.tala.user.dto.RegisterRequest;
import com.tala.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new TalaException(ErrorCode.USER_ALREADY_EXISTS, 
                "User with email " + request.getEmail() + " already exists");
        }
        
        // Create user
        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(hashPassword(request.getPassword()))
            .fullName(request.getFullName())
            .build();
        
        user = userRepository.save(user);
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        
        log.info("User registered successfully: {}", user.getEmail());
        
        return AuthResponse.of(token, user.getId(), user.getEmail(), user.getFullName());
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());
        
        // Find user
        User user = userRepository.findByEmailAndNotDeleted(request.getEmail())
            .orElseThrow(() -> new TalaException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid email or password"));
        
        // Verify password
        if (!verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new TalaException(ErrorCode.INVALID_CREDENTIALS, 
                "Invalid email or password");
        }
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        
        log.info("User logged in successfully: {}", user.getEmail());
        
        return AuthResponse.of(token, user.getId(), user.getEmail(), user.getFullName());
    }
    
    private String hashPassword(String password) {
        // Simple hash for now - in production use BCrypt
        return "hashed_" + password;
    }
    
    private boolean verifyPassword(String password, String passwordHash) {
        return passwordHash.equals("hashed_" + password);
    }
}
