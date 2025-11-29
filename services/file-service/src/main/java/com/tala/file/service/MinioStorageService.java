package com.tala.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * MinIO Storage Service
 * Placeholder implementation - in production, integrate with actual MinIO client
 */
@Service
@Slf4j
public class MinioStorageService {
    
    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;
    
    @Value("${minio.bucket:tala-files}")
    private String bucketName;
    
    // In-memory storage for testing (replace with actual MinIO in production)
    private final Map<String, byte[]> storage = new HashMap<>();
    
    public String uploadFile(InputStream inputStream, String storageKey, String contentType) throws Exception {
        log.info("Uploading file to MinIO: {}", storageKey);
        
        // Read all bytes (for in-memory storage)
        byte[] bytes = inputStream.readAllBytes();
        storage.put(storageKey, bytes);
        
        String storagePath = bucketName + "/" + storageKey;
        log.info("File uploaded successfully: {}", storagePath);
        
        return storagePath;
    }
    
    public InputStream getFileStream(String storageKey) throws Exception {
        log.debug("Getting file stream for: {}", storageKey);
        
        byte[] bytes = storage.get(storageKey);
        if (bytes == null) {
            throw new RuntimeException("File not found in storage: " + storageKey);
        }
        
        return new ByteArrayInputStream(bytes);
    }
    
    public String getFileUrl(String storageKey) {
        return minioEndpoint + "/" + bucketName + "/" + storageKey;
    }
    
    public void deleteFile(String storageKey) throws Exception {
        log.info("Deleting file from MinIO: {}", storageKey);
        storage.remove(storageKey);
    }
}
