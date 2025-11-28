package com.tala.core.domain;

import com.tala.core.util.IdGenerator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Base class for all entities
 * 
 * Provides:
 * - Snowflake ID generation
 * - Audit timestamps
 * - Soft delete support
 * 
 * @author Tala Backend Team
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    protected Long id;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;
    
    @Column(name = "deleted_at")
    protected Instant deletedAt;
    
    /**
     * Pre-persist hook to set ID and timestamps
     */
    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = IdGenerator.getInstance().nextId();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
    
    /**
     * Pre-update hook to update timestamp
     */
    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
    
    /**
     * Soft delete the entity
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }
    
    /**
     * Check if entity is deleted
     * 
     * @return true if entity is soft-deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    /**
     * Restore a soft-deleted entity
     */
    public void restore() {
        this.deletedAt = null;
    }
}
