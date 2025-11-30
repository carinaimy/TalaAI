package com.tala.user.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Baby profile entity
 */
@Entity
@Table(name = "profiles", schema = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "baby_name", nullable = false)
    private String babyName;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "timezone")
    @Builder.Default
    private String timezone = "UTC";
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "photo_url")
    private String photoUrl;
    
    @Column(name = "parent_name")
    private String parentName;
    
    @Column(name = "parent_role")
    private String parentRole;
    
    @Column(name = "zipcode")
    private String zipcode;
    
    @Column(name = "concerns")
    private String concerns;
    
    @Column(name = "has_daycare")
    @Builder.Default
    private Boolean hasDaycare = false;
    
    @Column(name = "daycare_name")
    private String daycareName;
    
    @Column(name = "update_method")
    private String updateMethod;
    
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
