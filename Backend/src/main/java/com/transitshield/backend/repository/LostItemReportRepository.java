package com.transitshield.backend.repository;

import com.transitshield.backend.entity.LostItemReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemReportRepository extends JpaRepository<LostItemReport, Long> {
}
