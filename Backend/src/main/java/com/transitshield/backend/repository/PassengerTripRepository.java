package com.transitshield.backend.repository;

import com.transitshield.backend.entity.PassengerTrip;
import com.transitshield.backend.entity.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PassengerTripRepository extends JpaRepository<PassengerTrip, Long> {
    Optional<PassengerTrip> findByTripRef(String tripRef);
    Optional<PassengerTrip> findByPassengerProfileIdAndTripStatus(Long passengerProfileId, TripStatus tripStatus);
    List<PassengerTrip> findByPassengerProfileId(Long passengerProfileId);
    List<PassengerTrip> findByBusAssignmentIdAndTripStatus(Long busAssignmentId, TripStatus tripStatus);
    long countByBusAssignmentDriverProfileIdAndCreatedAtBetween(Long driverProfileId, LocalDateTime start, LocalDateTime end);
}
