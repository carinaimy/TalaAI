package com.tala.user.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Link between profile (child) and care provider
 */
@Entity
@Table(
    name = "profile_care_provider_links",
    schema = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_provider", columnNames = {"profile_id", "care_provider_id"})
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCareProviderLink extends BaseEntity {
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Column(name = "care_provider_id", nullable = false)
    private Long careProviderId;
    
    @Column(name = "role", length = 20)
    @Builder.Default
    private String role = "PRIMARY";
    
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";
}
