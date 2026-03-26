package com.transitshield.backend.controller;

import com.transitshield.backend.dto.BusAssignmentDto;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.RouteVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/assignments")
@RequiredArgsConstructor
public class AdminBusAssignmentController {

    private final BusAssignmentRepository busAssignmentRepository;
    private final BusRepository busRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final RouteVariantRepository routeVariantRepository;

    @GetMapping
    public ResponseEntity<List<BusAssignmentDto>> getAllAssignments() {
        return ResponseEntity.ok(busAssignmentRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<BusAssignmentDto> createAssignment(@RequestBody BusAssignmentDto dto) {
        Bus bus = busRepository.findById(dto.getBusId()).orElseThrow(() -> new RuntimeException("Bus not found"));
        DriverProfile driver = driverProfileRepository.findById(dto.getDriverProfileId()).orElseThrow(() -> new RuntimeException("Driver not found"));
        RouteVariant routeVariant = routeVariantRepository.findById(dto.getRouteVariantId()).orElseThrow(() -> new RuntimeException("Route Variant not found"));

        // Deactivate existing assignments for this bus
        List<BusAssignment> existingBusAssignments = busAssignmentRepository.findByBusIdAndAssignmentStatus(bus.getId(), AssignmentStatus.ACTIVE);
        existingBusAssignments.forEach(a -> {
            a.setAssignmentStatus(AssignmentStatus.COMPLETED);
            a.setEndedAt(LocalDateTime.now());
            busAssignmentRepository.save(a);
        });

        BusAssignment assignment = new BusAssignment();
        assignment.setBus(bus);
        assignment.setDriverProfile(driver);
        assignment.setRouteVariant(routeVariant);
        assignment.setAssignmentStatus(AssignmentStatus.ACTIVE);
        assignment.setStartedAt(LocalDateTime.now());

        BusAssignment saved = busAssignmentRepository.save(assignment);
        return ResponseEntity.ok(mapToDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusAssignmentDto> updateAssignment(@PathVariable Long id, @RequestBody BusAssignmentDto dto) {
        return busAssignmentRepository.findById(id)
                .map(assignment -> {
                    if (dto.getBusId() != null) {
                        Bus bus = busRepository.findById(dto.getBusId())
                                .orElseThrow(() -> new RuntimeException("Bus not found"));
                        assignment.setBus(bus);
                    }
                    if (dto.getDriverProfileId() != null) {
                        DriverProfile driver = driverProfileRepository.findById(dto.getDriverProfileId())
                                .orElseThrow(() -> new RuntimeException("Driver not found"));
                        assignment.setDriverProfile(driver);
                    }
                    if (dto.getRouteVariantId() != null) {
                        RouteVariant routeVariant = routeVariantRepository.findById(dto.getRouteVariantId())
                                .orElseThrow(() -> new RuntimeException("Route Variant not found"));
                        assignment.setRouteVariant(routeVariant);
                    }
                    if (dto.getAssignmentStatus() != null) {
                        assignment.setAssignmentStatus(dto.getAssignmentStatus());
                    }
                    BusAssignment saved = busAssignmentRepository.save(assignment);
                    return ResponseEntity.ok(mapToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        if (!busAssignmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        busAssignmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private BusAssignmentDto mapToDto(BusAssignment entity) {
        BusAssignmentDto dto = new BusAssignmentDto();
        dto.setId(entity.getId());
        dto.setBusId(entity.getBus().getId());
        dto.setDriverProfileId(entity.getDriverProfile().getId());
        dto.setRouteVariantId(entity.getRouteVariant().getId());
        dto.setAssignmentStatus(entity.getAssignmentStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        return dto;
    }
}
