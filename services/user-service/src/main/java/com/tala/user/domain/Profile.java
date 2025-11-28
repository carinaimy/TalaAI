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
}
