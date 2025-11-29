package com.tala.user.repository;

import com.tala.user.domain.UserInterestProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserInterestProfile
 */
@Repository
public interface UserInterestProfileRepository extends JpaRepository<UserInterestProfile, Long> {
    
    Optional<UserInterestProfile> findByUserIdAndProfileId(Long userId, Long profileId);
}
