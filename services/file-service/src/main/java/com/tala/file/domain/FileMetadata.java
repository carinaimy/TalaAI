package com.tala.file.domain;

import com.tala.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * File metadata entity
 */
@Entity
@Table(
    name = "file_metadata",
    schema = "files",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_profile_id", columnList = "profile_id"),
        @Index(name = "idx_file_type", columnList = "file_type"),
        @Index(name = "idx_storage_key", columnList = "storage_key")
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "profile_id")
    private Long profileId;
    
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;
    
    @Column(name = "file_type", length = 50)
    private String fileType;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "storage_path")
    private String storagePath;
    
    @Column(name = "public_url", columnDefinition = "TEXT")
    private String publicUrl;
    
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;
    
    @Column(name = "width")
    private Integer width;
    
    @Column(name = "height")
    private Integer height;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "checksum", length = 64)
    private String checksum;
}
