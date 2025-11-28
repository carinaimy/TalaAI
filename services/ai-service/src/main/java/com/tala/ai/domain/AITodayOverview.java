package com.tala.ai.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * AI-generated Today At a Glance content
 */
@Entity
@Table(
    name = "ai_today_overviews",
    schema = "ai",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_date", columnNames = {"profile_id", "date"})
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AITodayOverview extends BaseEntity {
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "summary_sentence", columnDefinition = "TEXT")
    private String summarySentence;
    
    @Column(name = "action_suggestion", columnDefinition = "TEXT")
    private String actionSuggestion;
    
    @Type(JsonBinaryType.class)
    @Column(name = "pill_topics", columnDefinition = "jsonb")
    private List<PillTopic> pillTopics;
    
    @Column(name = "generated_at")
    private Instant generatedAt;
    
    @Column(name = "model_version")
    private String modelVersion;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PillTopic {
        private String title;
        private String topic;
        private String priority;
        private String description;
    }
}
