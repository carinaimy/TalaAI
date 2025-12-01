package com.tala.origindata.service;

import com.tala.core.dto.AttachmentRef;
import com.tala.origindata.client.FileServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves attachment file IDs to AttachmentRef DTOs
 * 
 * Calls file-service to get metadata and constructs unified attachment references.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentResolverService {
    
    private final FileServiceClient fileServiceClient;
    
    /**
     * Resolve file IDs to attachment references
     * 
     * @param fileIds List of file-service file IDs
     * @return List of AttachmentRef with URLs and metadata
     */
    public List<AttachmentRef> resolveAttachments(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return List.of();
        }
        
        List<AttachmentRef> attachments = new ArrayList<>();
        
        for (Long fileId : fileIds) {
            try {
                FileServiceClient.FileMetadataResponse metadata = fileServiceClient.getFileMetadata(fileId);
                
                AttachmentRef ref = AttachmentRef.builder()
                    .source("FILE_SERVICE")
                    .resourceId(metadata.id)
                    .url(metadata.publicUrl)
                    .thumbnailUrl(metadata.thumbnailUrl)
                    .mediaType(metadata.mimeType)
                    .label(metadata.originalFilename)
                    .sizeBytes(metadata.fileSize)
                    .build();
                
                attachments.add(ref);
                
            } catch (Exception e) {
                log.warn("Failed to resolve file ID {}: {}", fileId, e.getMessage());
                // Continue with other files, don't fail entire request
            }
        }
        
        return attachments;
    }
}
