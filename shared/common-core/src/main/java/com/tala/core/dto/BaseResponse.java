package com.tala.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Base Response DTO
 * 
 * Provides standard fields with proper JSON serialization:
 * - Long IDs serialized as strings to prevent JavaScript precision loss
 * - Instant timestamps in ISO-8601 format
 * 
 * JavaScript Number.MAX_SAFE_INTEGER = 2^53 - 1 = 9,007,199,254,740,991
 * Snowflake IDs exceed this limit, causing precision loss.
 * 
 * Solution: Serialize all Long IDs as strings in JSON.
 * 
 * @author Tala Backend Team
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseResponse {
    
    /**
     * Entity ID (Snowflake)
     * Serialized as string to prevent JavaScript precision loss
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    protected Long id;
    
    /**
     * Creation timestamp
     * Serialized in ISO-8601 format with milliseconds and Z timezone
     */
    protected Instant createdAt;
    
    /**
     * Last update timestamp
     * Serialized in ISO-8601 format with milliseconds and Z timezone
     */
    protected Instant updatedAt;
}
