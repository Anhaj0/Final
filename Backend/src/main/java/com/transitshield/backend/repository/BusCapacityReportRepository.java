package com.transitshield.backend.repository;

import com.transitshield.backend.entity.BusCapacityReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusCapacityReportRepository extends JpaRepository<BusCapacityReport, Long> {
    List<BusCapacityReport> findTop20ByBusIdOrderByCreatedAtDesc(Long busId);
}
