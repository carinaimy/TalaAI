package com.tala.media.domain;

import com.tala.core.domain.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.List;

/**
 * Media item entity for photos/videos
 */
@Entity
@Table(
    name = "media_items",
    schema = "media",
    indexes = {
        @Index(name = "idx_profile_occurred", columnList = "profile_id, occurred_at"),
        @Index(name = "idx_media_type", columnList = "media_type"),
        @Index(name = "idx_source", columnList = "source")
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem extends BaseEntity {
    
    @Column(name = "profile_id", nullable = false)
    private Long profileId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "care_provider_id")
    private Long careProviderId;
    
    @Column(name = "source", length = 50)
    @Builder.Default
    private String source = "USER_UPLOADED";
    
    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType;
    
    @Column(name = "storage_url", nullable = false)
    private String storageUrl;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "occurred_at")
    private Instant occurredAt;
    
    @Type(JsonBinaryType.class)
    @Column(name = "ai_tags", columnDefinition = "jsonb")
    private List<String> aiTags;
    
    @Column(name = "faces_count")
    private Integer facesCount;
    
    @Type(JsonBinaryType.class)
    @Column(name = "emotion_score", columnDefinition = "jsonb")
    private java.util.Map<String, Object> emotionScore;
}
