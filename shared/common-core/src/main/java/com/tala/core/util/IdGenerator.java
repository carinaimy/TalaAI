package com.tala.core.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Twitter Snowflake ID Generator
 * 
 * Generates unique 64-bit IDs across distributed systems
 * 
 * 64-bit ID structure:
 * - 1 bit: Sign (always 0)
 * - 41 bits: Timestamp (milliseconds since custom epoch)
 * - 10 bits: Worker ID (supports 1024 workers)
 * - 12 bits: Sequence (4096 IDs per millisecond per worker)
 * 
 * Features:
 * - Roughly time-ordered
 * - No coordination required between workers
 * - 4+ million IDs per second per worker
 * 
 * @author Tala Backend Team
 */
@Slf4j
public class IdGenerator {
    
    // Custom epoch: 2024-01-01 00:00:00 UTC
    private static final long EPOCH = 1704067200000L;
    
    // Bit allocations
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    
    // Max values
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    
    // Bit shifts
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    
    private final long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    private static volatile IdGenerator instance;
    
    /**
     * Private constructor
     * 
     * @param workerId Worker ID (0-1023)
     */
    private IdGenerator(long workerId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        this.workerId = workerId;
        log.info("IdGenerator initialized with workerId: {}", workerId);
    }
    
    /**
     * Get singleton instance
     * Worker ID is read from system property "tala.worker.id" or defaults to 0
     * 
     * @return IdGenerator instance
     */
    public static IdGenerator getInstance() {
        if (instance == null) {
            synchronized (IdGenerator.class) {
                if (instance == null) {
                    long workerId = Long.parseLong(
                        System.getProperty("tala.worker.id", "0")
                    );
                    instance = new IdGenerator(workerId);
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize with specific worker ID
     * Useful for testing
     * 
     * @param workerId Worker ID
     * @return IdGenerator instance
     */
    public static IdGenerator initialize(long workerId) {
        synchronized (IdGenerator.class) {
            instance = new IdGenerator(workerId);
        }
        return instance;
    }
    
    /**
     * Generate next unique ID
     * Thread-safe
     * 
     * @return Unique 64-bit ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        
        // Clock moved backwards
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                // Wait for clock to catch up (max 5ms)
                try {
                    wait(offset << 1);
                    timestamp = System.currentTimeMillis();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException(
                            String.format("Clock moved backwards by %dms", offset));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for clock", e);
                }
            } else {
                throw new RuntimeException(
                    String.format("Clock moved backwards by %dms", offset));
            }
        }
        
        // Same millisecond - increment sequence
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // Sequence overflow - wait for next millisecond
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // New millisecond - reset sequence
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        // Generate ID
        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT) |
               (workerId << WORKER_ID_SHIFT) |
               sequence;
    }
    
    /**
     * Wait for next millisecond
     * 
     * @param lastTimestamp Last timestamp
     * @return New timestamp
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
    
    /**
     * Parse ID to extract components
     * Useful for debugging
     * 
     * @param id ID to parse
     * @return Array [timestamp, workerId, sequence]
     */
    public static long[] parseId(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        long workerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long sequence = id & MAX_SEQUENCE;
        return new long[]{timestamp, workerId, sequence};
    }
}
