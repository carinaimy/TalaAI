package com.tala.file.repository;

import com.tala.file.domain.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<FileMetadata> findByProfileIdOrderByCreatedAtDesc(Long profileId);
}
