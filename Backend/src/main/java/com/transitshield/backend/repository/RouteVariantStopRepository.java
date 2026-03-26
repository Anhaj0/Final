package com.transitshield.backend.repository;

import com.transitshield.backend.entity.RouteVariantStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteVariantStopRepository extends JpaRepository<RouteVariantStop, Long> {
    List<RouteVariantStop> findByRouteVariantIdOrderByStopOrderAsc(Long routeVariantId);
}
