package com.transitshield.backend.repository;

import com.transitshield.backend.entity.TripExtension;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripExtensionRepository extends JpaRepository<TripExtension, Long> {
}
