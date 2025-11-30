package com.tala.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for partial profile updates (PATCH-style)
 * All fields are optional and only non-null values will be applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    private String babyName;
    private LocalDate birthDate;
    private String timezone;
    private String gender;
    private String photoUrl;
    private String parentName;
    private String parentRole;
    private String zipcode;
    private String concerns;
    private Boolean hasDaycare;
    private String daycareName;
    private String updateMethod;
}
