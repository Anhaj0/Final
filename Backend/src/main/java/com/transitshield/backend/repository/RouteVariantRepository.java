package com.transitshield.backend.repository;

import com.transitshield.backend.entity.RouteVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteVariantRepository extends JpaRepository<RouteVariant, Long> {
}
