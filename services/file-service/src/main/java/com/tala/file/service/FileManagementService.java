package com.tala.file.service;

import com.tala.file.domain.FileMetadata;
import com.tala.file.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileManagementService {
    
    private final FileMetadataRepository repository;
    private final MinioStorageService storageService;
    
    @Transactional
    public FileMetadata uploadFile(MultipartFile file, Long userId, Long profileId) throws Exception {
        log.info("Uploading file: {} for user: {}", file.getOriginalFilename(), userId);
        
        // Generate storage key
        String storageKey = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        // Upload to MinIO
        String storagePath = storageService.uploadFile(file.getInputStream(), storageKey, file.getContentType());
        
        // Create metadata
        FileMetadata metadata = FileMetadata.builder()
            .userId(userId)
            .profileId(profileId)
            .originalFilename(file.getOriginalFilename())
            .storageKey(storageKey)
            .fileType(determineFileType(file.getContentType()))
            .mimeType(file.getContentType())
            .fileSize(file.getSize())
            .storagePath(storagePath)
            .publicUrl(storageService.getFileUrl(storageKey))
            .checksum(calculateChecksum(file))
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
        
        return repository.save(metadata);
    }
    
    public Optional<FileMetadata> getFileMetadata(Long fileId) {
        return repository.findById(fileId);
    }
    
    public InputStream getFileStream(String storageKey) throws Exception {
        return storageService.getFileStream(storageKey);
    }
    
    public List<FileMetadata> getUserFiles(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<FileMetadata> getProfileFiles(Long profileId) {
        return repository.findByProfileIdOrderByCreatedAtDesc(profileId);
    }
    
    @Transactional
    public void deleteFile(Long fileId) throws Exception {
        FileMetadata metadata = repository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
        
        // Delete from storage
        storageService.deleteFile(metadata.getStorageKey());
        
        // Delete metadata
        repository.delete(metadata);
    }
    
    private String determineFileType(String mimeType) {
        if (mimeType == null) return "OTHER";
        if (mimeType.startsWith("image/")) return "IMAGE";
        if (mimeType.startsWith("video/")) return "VIDEO";
        if (mimeType.startsWith("audio/")) return "AUDIO";
        if (mimeType.contains("pdf")) return "PDF";
        return "OTHER";
    }
    
    private String calculateChecksum(MultipartFile file) {
        // Simple checksum - in production use MD5 or SHA256
        try {
            return String.valueOf(file.getBytes().length);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
