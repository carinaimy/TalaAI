package com.tala.core.constant;

import java.util.Arrays;
import java.util.List;

/**
 * All event type constants
 * 
 * @author Tala Backend Team
 */
public final class EventTypes {
    
    // Food & Feeding
    public static final String FEEDING = "FEEDING";
    public static final String PUMPING = "PUMPING";
    public static final String BOTTLE = "BOTTLE";
    public static final String SOLID_FOOD = "SOLID_FOOD";
    
    // Sleep
    public static final String SLEEP = "SLEEP";
    public static final String NAP = "NAP";
    public static final String BEDTIME = "BEDTIME";
    
    // Diaper & Potty
    public static final String DIAPER = "DIAPER";
    public static final String POTTY = "POTTY";
    
    // Health
    public static final String TEMPERATURE = "TEMPERATURE";
    public static final String SYMPTOM = "SYMPTOM";
    public static final String MEDICATION = "MEDICATION";
    public static final String VACCINATION = "VACCINATION";
    public static final String MEDICAL_VISIT = "MEDICAL_VISIT";
    public static final String GROWTH = "GROWTH";
    
    // Development
    public static final String MILESTONE = "MILESTONE";
    public static final String SKILL = "SKILL";
    
    // Activity
    public static final String ACTIVITY = "ACTIVITY";
    public static final String TUMMY_TIME = "TUMMY_TIME";
    public static final String PLAY_TIME = "PLAY_TIME";
    public static final String OUTDOOR = "OUTDOOR";
    
    // Mood & Behavior
    public static final String MOOD = "MOOD";
    public static final String BEHAVIOR = "BEHAVIOR";
    public static final String CRYING = "CRYING";
    
    // Daycare
    public static final String DAYCARE_EVENT = "DAYCARE_EVENT";
    public static final String DAYCARE_DROP_OFF = "DAYCARE_DROP_OFF";
    public static final String DAYCARE_PICK_UP = "DAYCARE_PICK_UP";
    
    // Other
    public static final String BATH = "BATH";
    public static final String NOTE = "NOTE";
    public static final String PHOTO = "PHOTO";
    
    /**
     * All valid event types
     */
    public static final List<String> ALL_TYPES = Arrays.asList(
        FEEDING, PUMPING, BOTTLE, SOLID_FOOD,
        SLEEP, NAP, BEDTIME,
        DIAPER, POTTY,
        TEMPERATURE, SYMPTOM, MEDICATION, VACCINATION, MEDICAL_VISIT, GROWTH,
        MILESTONE, SKILL,
        ACTIVITY, TUMMY_TIME, PLAY_TIME, OUTDOOR,
        MOOD, BEHAVIOR, CRYING,
        DAYCARE_EVENT, DAYCARE_DROP_OFF, DAYCARE_PICK_UP,
        BATH, NOTE, PHOTO
    );
    
    private EventTypes() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Check if event type is valid
     * 
     * @param eventType Event type to check
     * @return true if valid
     */
    public static boolean isValid(String eventType) {
        return eventType != null && ALL_TYPES.contains(eventType);
    }
}
