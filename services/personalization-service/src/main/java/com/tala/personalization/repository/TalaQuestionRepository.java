package com.tala.personalization.repository;

import com.tala.personalization.domain.TalaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TalaQuestion
 */
@Repository
public interface TalaQuestionRepository extends JpaRepository<TalaQuestion, Long> {
    
    List<TalaQuestion> findByQuestionTypeAndIsActiveTrue(String questionType);
    
    List<TalaQuestion> findByTopicAndIsActiveTrue(String topic);
    
    @Query("SELECT q FROM TalaQuestion q WHERE q.isActive = true " +
           "AND q.questionType = :questionType " +
           "AND q.minAgeMonths <= :ageMonths " +
           "AND (q.maxAgeMonths IS NULL OR q.maxAgeMonths >= :ageMonths)")
    List<TalaQuestion> findAgeAppropriateQuestions(
        @Param("questionType") String questionType,
        @Param("ageMonths") Integer ageMonths
    );
    
    @Query("SELECT q FROM TalaQuestion q WHERE q.isActive = true " +
           "AND q.topic = :topic " +
           "AND q.minAgeMonths <= :ageMonths " +
           "AND (q.maxAgeMonths IS NULL OR q.maxAgeMonths >= :ageMonths)")
    List<TalaQuestion> findAgeAppropriateQuestionsByTopic(
        @Param("topic") String topic,
        @Param("ageMonths") Integer ageMonths
    );
}
