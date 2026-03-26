package com.transitshield.backend.repository;

import com.transitshield.backend.entity.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {
    Optional<FareRule> findByRouteVariantIdAndBoardingStopIdAndDestinationStopId(Long routeVariantId, Long boardingStopId, Long destinationStopId);
}
