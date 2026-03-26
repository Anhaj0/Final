package com.transitshield.backend.repository;

import com.transitshield.backend.entity.RewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {
    List<RewardTransaction> findByPassengerProfileIdOrderByCreatedAtDesc(Long passengerProfileId);
}
