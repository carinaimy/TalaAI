package com.tala.personalization.repository;

import com.tala.personalization.domain.UserInterestProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserInterestProfile
 */
@Repository
public interface UserInterestProfileRepository extends JpaRepository<UserInterestProfile, Long> {
    
    Optional<UserInterestProfile> findByUserIdAndProfileId(Long userId, Long profileId);
    
    boolean existsByUserIdAndProfileId(Long userId, Long profileId);
}
