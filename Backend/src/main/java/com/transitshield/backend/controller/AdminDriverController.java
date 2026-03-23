package com.transitshield.backend.controller;

import com.transitshield.backend.dto.DriverProfileDto;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.BusQrCode;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusQrCodeRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.transitshield.backend.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/driver-profiles")
@RequiredArgsConstructor
public class AdminDriverController {

    private final DriverProfileRepository driverProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final BusQrCodeRepository busQrCodeRepository;

    @GetMapping
    public ResponseEntity<List<DriverProfileDto>> getAllDriverProfiles() {
        List<DriverProfileDto> dtos = driverProfileRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DriverProfileDto> getByUserId(@PathVariable("userId") Long userId) {
        DriverProfile profile = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + userId));
        return ResponseEntity.ok(mapToDto(profile));
    }

    private DriverProfileDto mapToDto(DriverProfile profile) {
        DriverProfileDto dto = new DriverProfileDto();

        dto.setId(profile.getId());
        dto.setDriverCode(profile.getDriverCode());
        dto.setLicenseNumber(profile.getLicenseNumber());
        dto.setDepot(profile.getDepot());
        dto.setDemeritPoints(profile.getDemeritPoints());
        dto.setStatus(profile.getStatus());

        User user = profile.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setAge(user.getAge());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setIsActive(user.getIsActive());
        }

        BusAssignment activeAssignment = busAssignmentRepository
                .findFirstByDriverProfileIdAndAssignmentStatusOrderByStartedAtDesc(
                        profile.getId(),
                        AssignmentStatus.ACTIVE
                )
                .orElse(null);

        if (activeAssignment != null) {
            dto.setActiveAssignmentId(activeAssignment.getId());

            if (activeAssignment.getBus() != null) {
                dto.setAssignedBusId(activeAssignment.getBus().getId());
                dto.setAssignedBusCode(activeAssignment.getBus().getBusCode());
                dto.setAssignedBusDisplayName(activeAssignment.getBus().getBusDisplayName());
                dto.setAssignedBusRegistrationNumber(activeAssignment.getBus().getRegistrationNumber());

                BusQrCode activeQr = busQrCodeRepository
                        .findByBusIdAndIsActiveTrue(activeAssignment.getBus().getId())
                        .orElse(null);

                dto.setHasActiveQr(activeQr != null);
                if (activeQr != null) {
                    dto.setActiveQrLabel(activeQr.getQrLabel());
                }
            } else {
                dto.setHasActiveQr(false);
            }

            RouteVariant routeVariant = activeAssignment.getRouteVariant();
            if (routeVariant != null) {
                dto.setAssignedRouteVariantId(routeVariant.getId());
                dto.setAssignedRouteVariantCode(routeVariant.getVariantCode());
                dto.setOriginName(routeVariant.getOriginName());
                dto.setDestinationName(routeVariant.getDestinationName());
                dto.setDirectionLabel(routeVariant.getDirectionLabel());

                if (routeVariant.getRoute() != null) {
                    dto.setAssignedRouteNumber(routeVariant.getRoute().getRouteNumber());
                    dto.setAssignedRouteName(routeVariant.getRoute().getDisplayName());
                }
            }
        } else {
            dto.setHasActiveQr(false);
        }

        return dto;
    }
}
