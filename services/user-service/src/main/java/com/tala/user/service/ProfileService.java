package com.tala.user.service;

import com.tala.core.exception.ErrorCode;
import com.tala.core.exception.TalaException;
import com.tala.user.domain.Profile;
import com.tala.user.dto.ProfileRequest;
import com.tala.user.dto.ProfileResponse;
import com.tala.user.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    
    @Transactional
    public ProfileResponse createProfile(Long userId, ProfileRequest request) {
        log.info("Creating profile for user: {}", userId);
        
        Profile profile = Profile.builder()
            .userId(userId)
            .babyName(request.getBabyName())
            .birthDate(request.getBirthDate())
            .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
            .gender(request.getGender())
            .photoUrl(request.getPhotoUrl())
            .build();
        
        profile = profileRepository.save(profile);
        
        return toResponse(profile);
    }
    
    @Transactional(readOnly = true)
    public List<ProfileResponse> getUserProfiles(Long userId) {
        List<Profile> profiles = profileRepository.findByUserIdAndNotDeleted(userId);
        return profiles.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long profileId) {
        Profile profile = profileRepository.findByIdAndNotDeleted(profileId)
            .orElseThrow(() -> new TalaException(ErrorCode.USER_NOT_FOUND, 
                "Profile not found"));
        return toResponse(profile);
    }
    
    @Transactional
    public ProfileResponse updateProfile(Long profileId, ProfileRequest request) {
        Profile profile = profileRepository.findByIdAndNotDeleted(profileId)
            .orElseThrow(() -> new TalaException(ErrorCode.USER_NOT_FOUND, 
                "Profile not found"));
        
        if (request.getBabyName() != null) {
            profile.setBabyName(request.getBabyName());
        }
        if (request.getBirthDate() != null) {
            profile.setBirthDate(request.getBirthDate());
        }
        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getPhotoUrl() != null) {
            profile.setPhotoUrl(request.getPhotoUrl());
        }
        
        profile = profileRepository.save(profile);
        
        return toResponse(profile);
    }
    
    @Transactional
    public void deleteProfile(Long profileId) {
        Profile profile = profileRepository.findByIdAndNotDeleted(profileId)
            .orElseThrow(() -> new TalaException(ErrorCode.USER_NOT_FOUND, 
                "Profile not found"));
        
        profile.softDelete();
        profileRepository.save(profile);
    }
    
    private ProfileResponse toResponse(Profile profile) {
        Integer ageInDays = null;
        if (profile.getBirthDate() != null) {
            ageInDays = (int) ChronoUnit.DAYS.between(profile.getBirthDate(), LocalDate.now());
        }
        
        return ProfileResponse.builder()
            .id(profile.getId())
            .userId(profile.getUserId())
            .babyName(profile.getBabyName())
            .birthDate(profile.getBirthDate())
            .timezone(profile.getTimezone())
            .gender(profile.getGender())
            .photoUrl(profile.getPhotoUrl())
            .ageInDays(ageInDays)
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
}
