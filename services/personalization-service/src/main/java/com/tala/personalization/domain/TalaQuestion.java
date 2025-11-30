package com.tala.personalization.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.util.Map;

/**
 * Tala Question Entity
 * Stores conversation starters and check-in questions
 */
@Entity
@Table(
    name = "tala_questions",
    schema = "personalization",
    indexes = {
        @Index(name = "idx_tala_questions_topic", columnList = "topic"),
        @Index(name = "idx_tala_questions_type", columnList = "question_type"),
        @Index(name = "idx_tala_questions_age", columnList = "min_age_months, max_age_months"),
        @Index(name = "idx_tala_questions_active", columnList = "is_active")
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TalaQuestion extends BaseEntity {
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @Column(name = "question_type", length = 50, nullable = false)
    private String questionType; // ask_baby_about/daytime_checkin/conversation_starter
    
    @Column(name = "topic", length = 50, nullable = false)
    private String topic; // food/sleep/health/development/social/activity
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "min_age_months", nullable = false)
    @Builder.Default
    private Integer minAgeMonths = 0;
    
    @Column(name = "max_age_months")
    private Integer maxAgeMonths;
    
    @Column(name = "requires_daycare")
    @Builder.Default
    private Boolean requiresDaycare = false;
    
    @Column(name = "requires_incident")
    @Builder.Default
    private Boolean requiresIncident = false;
    
    @Column(name = "requires_event_type", length = 50)
    private String requiresEventType;
    
    @Column(name = "answer_type", length = 50, nullable = false)
    private String answerType; // choice/scale/boolean/text/number
    
    @Type(JsonBinaryType.class)
    @Column(name = "answer_choices", columnDefinition = "jsonb")
    private Map<String, Object> answerChoices;
    
    @Column(name = "base_priority")
    @Builder.Default
    private Integer basePriority = 50;
    
    @Column(name = "max_frequency_days")
    @Builder.Default
    private Integer maxFrequencyDays = 7;
    
    @Type(StringArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
