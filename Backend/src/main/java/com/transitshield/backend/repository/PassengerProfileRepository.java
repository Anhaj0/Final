package com.transitshield.backend.repository;

import com.transitshield.backend.entity.PassengerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PassengerProfileRepository extends JpaRepository<PassengerProfile, Long> {
    Optional<PassengerProfile> findByPublicUserId(String publicUserId);
    Optional<PassengerProfile> findByUserId(Long userId);
}
