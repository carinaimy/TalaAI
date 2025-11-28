package com.tala.user.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.util.List;

/**
 * Checkin question for daily interaction
 */
@Entity
@Table(name = "checkin_questions", schema = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinQuestion extends BaseEntity {
    
    @Column(name = "age_min_months")
    private Integer ageMinMonths;
    
    @Column(name = "age_max_months")
    private Integer ageMaxMonths;
    
    @Column(name = "topic", length = 50)
    private String topic;
    
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Column(name = "answer_type", length = 20)
    private String answerType;
    
    @Type(JsonBinaryType.class)
    @Column(name = "choices", columnDefinition = "jsonb")
    private List<String> choices;
    
    @Column(name = "frequency_hint", length = 20)
    private String frequencyHint;
    
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;
}
