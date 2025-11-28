package com.tala.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All error codes used in the system
 * 
 * @author Tala Backend Team
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // General errors (1xxx)
    INTERNAL_ERROR(1000, "Internal server error"),
    VALIDATION_ERROR(1001, "Validation failed"),
    NOT_FOUND(1002, "Resource not found"),
    ALREADY_EXISTS(1003, "Resource already exists"),
    UNAUTHORIZED(1004, "Unauthorized"),
    FORBIDDEN(1005, "Forbidden"),
    BAD_REQUEST(1006, "Bad request"),
    CONFLICT(1007, "Resource conflict"),
    
    // Event errors (2xxx)
    INVALID_EVENT_TYPE(2000, "Invalid event type"),
    INVALID_EVENT_DATA(2001, "Invalid event data"),
    EVENT_NOT_FOUND(2002, "Event not found"),
    EVENT_ALREADY_EXISTS(2003, "Event already exists"),
    INVALID_EVENT_TIME(2004, "Invalid event time"),
    
    // User errors (3xxx)
    USER_NOT_FOUND(3000, "User not found"),
    PROFILE_NOT_FOUND(3001, "Profile not found"),
    USER_ALREADY_EXISTS(3002, "User already exists"),
    INVALID_CREDENTIALS(3003, "Invalid credentials"),
    EMAIL_NOT_VERIFIED(3004, "Email not verified"),
    
    // Query errors (4xxx)
    INVALID_QUERY_PARAMS(4000, "Invalid query parameters"),
    QUERY_TIMEOUT(4001, "Query timeout"),
    TOO_MANY_RESULTS(4002, "Too many results"),
    
    // AI Service errors (5xxx)
    AI_SERVICE_ERROR(5000, "AI service error"),
    INSUFFICIENT_DATA(5001, "Insufficient data for AI analysis"),
    AI_MODEL_ERROR(5002, "AI model error"),
    
    // External service errors (9xxx)
    EXTERNAL_SERVICE_ERROR(9000, "External service error"),
    KAFKA_ERROR(9001, "Kafka error"),
    CLICKHOUSE_ERROR(9002, "ClickHouse error"),
    REDIS_ERROR(9003, "Redis error"),
    DATABASE_ERROR(9004, "Database error"),
    NETWORK_ERROR(9005, "Network error");
    
    private final int code;
    private final String message;
}
