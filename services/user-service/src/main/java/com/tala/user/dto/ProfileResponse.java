package com.tala.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tala.core.dto.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Profile Response DTO
 * Extends BaseResponse for proper Long ID serialization
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProfileResponse extends BaseResponse {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    
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
    private Integer ageInDays;
    private Boolean isDefault;
}
