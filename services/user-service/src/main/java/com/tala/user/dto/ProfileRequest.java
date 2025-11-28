package com.tala.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {
    
    @NotBlank(message = "Baby name is required")
    private String babyName;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    private String timezone;
    private String gender;
    private String photoUrl;
}
