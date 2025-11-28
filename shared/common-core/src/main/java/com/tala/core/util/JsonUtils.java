package com.tala.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * JSON serialization/deserialization utilities
 * 
 * @author Tala Backend Team
 */
@Slf4j
public final class JsonUtils {
    
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Get configured ObjectMapper instance
     * 
     * @return ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
    
    /**
     * Convert object to JSON string
     * 
     * @param obj Object to serialize
     * @return JSON string
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
    
    /**
     * Convert object to pretty JSON string
     * 
     * @param obj Object to serialize
     * @return Pretty JSON string
     */
    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to pretty JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
    
    /**
     * Parse JSON string to object
     * 
     * @param json JSON string
     * @param clazz Target class
     * @param <T> Type parameter
     * @return Deserialized object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON to {}", clazz.getName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
    
    /**
     * Parse JSON string to object with TypeReference
     * 
     * @param json JSON string
     * @param typeRef Type reference
     * @param <T> Type parameter
     * @return Deserialized object
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (IOException e) {
            log.error("Failed to deserialize JSON", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }
    
    /**
     * Convert object to Map
     * 
     * @param obj Object to convert
     * @return Map representation
     */
    public static Map<String, Object> toMap(Object obj) {
        return MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }
    
    /**
     * Convert Map to object
     * 
     * @param map Map to convert
     * @param clazz Target class
     * @param <T> Type parameter
     * @return Object
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return MAPPER.convertValue(map, clazz);
    }
}
