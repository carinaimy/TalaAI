package com.tala.personalization.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * User Interest Profile Entity
 * Tracks user's topic interests and interaction patterns
 */
@Entity
@Table(
    name = "user_interest_profiles",
    schema = "personalization",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "profile_id"})
    },
    indexes = {
        @Index(name = "idx_interest_profiles_user", columnList = "user_id"),
        @Index(name = "idx_interest_profiles_profile", columnList = "profile_id"),
        @Index(name = "idx_interest_profiles_updated", columnList = "updated_at")
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterestProfile extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Type(JsonBinaryType.class)
    @Column(name = "interest_vector", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Double> interestVector = Map.of();
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "explicit_topics")
    private String[] explicitTopics;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "recent_topics")
    private String[] recentTopics;
    
    @Column(name = "daily_interaction_score")
    @Builder.Default
    private Integer dailyInteractionScore = 0;
    
    @Column(name = "last_interaction_at")
    private Instant lastInteractionAt;
}
