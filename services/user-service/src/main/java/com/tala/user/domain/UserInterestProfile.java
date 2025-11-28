package com.tala.user.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;

/**
 * User interest profile for personalization
 */
@Entity
@Table(
    name = "user_interest_profiles",
    schema = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_profile", columnNames = {"user_id", "profile_id"})
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
    @Column(name = "interest_vector", columnDefinition = "jsonb")
    private Map<String, Double> interestVector;
    
    @Type(JsonBinaryType.class)
    @Column(name = "explicit_topics", columnDefinition = "jsonb")
    private List<String> explicitTopics;
    
    @Type(JsonBinaryType.class)
    @Column(name = "recent_topics", columnDefinition = "jsonb")
    private List<String> recentTopics;
}
