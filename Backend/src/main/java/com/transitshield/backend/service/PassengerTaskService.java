package com.transitshield.backend.service;

import com.transitshield.backend.dto.PassengerTaskCapacityRequest;
import com.transitshield.backend.dto.PassengerTaskLocationRequest;
import com.transitshield.backend.dto.PassengerTaskResponse;
import com.transitshield.backend.entity.*;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.entity.enums.SourceType;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PassengerTaskService {

    private final BusRepository busRepository;
    private final BusLocationRepository busLocationRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final BusCapacityReportRepository busCapacityReportRepository;
    private final PointsTransactionRepository pointsTransactionRepository;

    @Transactional
    public PassengerTaskResponse reportLocation(User user, PassengerTaskLocationRequest request) {
        if (request == null || request.getBusId() == null || request.getLatitude() == null || request.getLongitude() == null) {
            throw new BadRequestException("busId, latitude and longitude are required");
        }

        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        PassengerProfile profile = passengerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger profile not found"));

        BusLocation location = new BusLocation();
        location.setBus(bus);

        busAssignmentRepository.findFirstByBusIdAndAssignmentStatusOrderByStartedAtDesc(bus.getId(), AssignmentStatus.ACTIVE)
                .ifPresent(assignment -> {
                    location.setRouteVariant(assignment.getRouteVariant());
                    location.setDriverProfile(assignment.getDriverProfile());
                });

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setSourceType(SourceType.PASSENGER_TASK);
        location.setRecordedAt(LocalDateTime.now());
        busLocationRepository.save(location);

        Double updatedPoints = addPoints(profile, bus, 5.0, "TASK_LOCATION", "Passenger location task completed");

        return new PassengerTaskResponse("Location report saved and points awarded", 5.0, updatedPoints, null);
    }

    @Transactional
    public PassengerTaskResponse reportCapacity(User user, PassengerTaskCapacityRequest request) {
        if (request == null || request.getBusId() == null || request.getPassengerCount() == null) {
            throw new BadRequestException("busId and passengerCount are required");
        }

        if (request.getPassengerCount() < 0) {
            throw new BadRequestException("passengerCount must be non-negative");
        }

        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        PassengerProfile profile = passengerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger profile not found"));

        List<BusCapacityReport> recent = busCapacityReportRepository.findTop20ByBusIdOrderByCreatedAtDesc(bus.getId());

        double sum = request.getPassengerCount();
        int count = 1;
        for (BusCapacityReport report : recent) {
            sum += report.getPassengerCount();
            count++;
        }
        double average = sum / count;

        BusCapacityReport report = new BusCapacityReport();
        report.setBus(bus);
        report.setReporterUser(user);
        report.setPassengerCount(request.getPassengerCount());
        report.setAverageCapacity(average);
        report.setCreatedAt(LocalDateTime.now());
        busCapacityReportRepository.save(report);

        Double updatedPoints = addPoints(profile, bus, 2.0, "TASK_CAPACITY", "Passenger capacity task completed");

        return new PassengerTaskResponse("Capacity report saved and points awarded", 2.0, updatedPoints, average);
    }

    private Double addPoints(PassengerProfile profile, Bus bus, Double points, String taskType, String description) {
        profile.setTotalPoints((profile.getTotalPoints() != null ? profile.getTotalPoints() : 0.0) + points);
        passengerProfileRepository.save(profile);

        PointsTransaction tx = new PointsTransaction();
        tx.setPassengerProfile(profile);
        tx.setBus(bus);
        tx.setPoints(points);
        tx.setTaskType(taskType);
        tx.setDescription(description);
        tx.setCreatedAt(LocalDateTime.now());
        pointsTransactionRepository.save(tx);

        return profile.getTotalPoints();
    }
}
