package com.transitshield.backend.repository;

import com.transitshield.backend.entity.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
}
