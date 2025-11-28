package com.tala.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    
    private Long id;
    private Long userId;
    private String babyName;
    private LocalDate birthDate;
    private String timezone;
    private String gender;
    private String photoUrl;
    private Integer ageInDays;
    private Instant createdAt;
    private Instant updatedAt;
}
