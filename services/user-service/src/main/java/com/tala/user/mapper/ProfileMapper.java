package com.tala.user.mapper;

import com.tala.user.domain.Profile;
import com.tala.user.dto.ProfileRequest;
import com.tala.user.dto.ProfileResponse;
import com.tala.user.dto.ProfileUpdateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mapper for Profile entity and DTOs
 */
@Component
public class ProfileMapper {
    
    /**
     * Convert Profile entity to ProfileResponse DTO
     */
    public ProfileResponse toResponse(Profile profile) {
        if (profile == null) {
            return null;
        }
        
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
            .parentName(profile.getParentName())
            .parentRole(profile.getParentRole())
            .zipcode(profile.getZipcode())
            .concerns(profile.getConcerns())
            .hasDaycare(profile.getHasDaycare())
            .daycareName(profile.getDaycareName())
            .updateMethod(profile.getUpdateMethod())
            .ageInDays(ageInDays)
            .isDefault(profile.getIsDefault())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
    
    /**
     * Update Profile entity from ProfileUpdateRequest DTO (PATCH-style)
     */
    public void updateFromRequest(Profile profile, ProfileUpdateRequest request) {
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
        if (request.getParentName() != null) {
            profile.setParentName(request.getParentName());
        }
        if (request.getParentRole() != null) {
            profile.setParentRole(request.getParentRole());
        }
        if (request.getZipcode() != null) {
            profile.setZipcode(request.getZipcode());
        }
        if (request.getConcerns() != null) {
            profile.setConcerns(request.getConcerns());
        }
        if (request.getHasDaycare() != null) {
            profile.setHasDaycare(request.getHasDaycare());
        }
        if (request.getDaycareName() != null) {
            profile.setDaycareName(request.getDaycareName());
        }
        if (request.getUpdateMethod() != null) {
            profile.setUpdateMethod(request.getUpdateMethod());
        }
    }
}
