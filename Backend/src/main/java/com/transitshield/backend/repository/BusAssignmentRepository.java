package com.transitshield.backend.repository;

import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BusAssignmentRepository extends JpaRepository<BusAssignment, Long> {
    List<BusAssignment> findByBusIdAndAssignmentStatus(Long busId, AssignmentStatus assignmentStatus);
    Optional<BusAssignment> findFirstByBusIdAndAssignmentStatusOrderByStartedAtDesc(Long busId, AssignmentStatus assignmentStatus);
    Optional<BusAssignment> findFirstByDriverProfileIdAndAssignmentStatusOrderByStartedAtDesc(Long driverProfileId, AssignmentStatus assignmentStatus);
}
