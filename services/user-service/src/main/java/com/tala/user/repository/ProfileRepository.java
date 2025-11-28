package com.tala.user.repository;

import com.tala.user.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    @Query("SELECT p FROM Profile p WHERE p.userId = :userId AND p.deletedAt IS NULL")
    List<Profile> findByUserIdAndNotDeleted(Long userId);
    
    @Query("SELECT p FROM Profile p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Profile> findByIdAndNotDeleted(Long id);
}
