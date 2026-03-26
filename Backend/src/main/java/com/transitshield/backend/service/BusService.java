package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusDto;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.RouteVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusService {
    private final BusRepository busRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final RouteVariantRepository routeVariantRepository;

    public List<BusDto> findAll() {
        return busRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public BusDto findById(Long id) {
        return busRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    @Transactional
    public BusDto create(BusDto dto) {
        DriverProfile driverProfile = resolveDriverProfile(dto.getDriverProfileId());
        Bus bus = mapToEntity(dto);
        bus.setOperatorName(resolveOperatorName(dto, driverProfile));
        bus = busRepository.save(bus);
        syncAssignment(bus, dto, driverProfile, null);
        return mapToDto(bus);
    }

    @Transactional
    public BusDto update(Long id, BusDto dto) {
        return busRepository.findById(id).map(bus -> {
            BusAssignment activeAssignment = findActiveAssignment(bus.getId()).orElse(null);
            DriverProfile driverProfile = resolveDriverProfile(dto.getDriverProfileId());

            bus.setBusCode(dto.getBusCode());
            bus.setRegistrationNumber(dto.getRegistrationNumber());
            bus.setBusDisplayName(dto.getBusDisplayName());
            bus.setCapacity(dto.getCapacity());
            bus.setStatus(dto.getStatus());
            bus.setOperatorName(resolveOperatorName(dto, driverProfile));

            Bus savedBus = busRepository.save(bus);
            syncAssignment(savedBus, dto, driverProfile, activeAssignment);
            return mapToDto(savedBus);
        }).orElse(null);
    }

    public void delete(Long id) {
        busRepository.deleteById(id);
    }

    private BusDto mapToDto(Bus bus) {
        BusDto dto = new BusDto();
        dto.setId(bus.getId());
        dto.setBusCode(bus.getBusCode());
        dto.setRegistrationNumber(bus.getRegistrationNumber());
        dto.setBusDisplayName(bus.getBusDisplayName());
        dto.setCapacity(bus.getCapacity());
        dto.setOperatorName(bus.getOperatorName());
        dto.setStatus(bus.getStatus());

        findActiveAssignment(bus.getId()).ifPresent(assignment -> {
            dto.setActiveAssignmentId(assignment.getId());
            dto.setDriverProfileId(assignment.getDriverProfile().getId());

            User driverUser = assignment.getDriverProfile().getUser();
            if (driverUser != null) {
                dto.setDriverFullName(driverUser.getFullName());
                dto.setOperatorName(driverUser.getFullName());
            }

            if (assignment.getRouteVariant() != null) {
                dto.setRouteVariantId(assignment.getRouteVariant().getId());
                dto.setRouteVariantLabel(buildRouteVariantLabel(assignment.getRouteVariant()));
            }
        });

        return dto;
    }

    private Bus mapToEntity(BusDto dto) {
        Bus bus = new Bus();
        bus.setId(dto.getId());
        bus.setBusCode(dto.getBusCode());
        bus.setRegistrationNumber(dto.getRegistrationNumber());
        bus.setBusDisplayName(dto.getBusDisplayName());
        bus.setCapacity(dto.getCapacity());
        bus.setOperatorName(dto.getOperatorName());
        bus.setStatus(dto.getStatus());
        return bus;
    }

    private Optional<BusAssignment> findActiveAssignment(Long busId) {
        return busAssignmentRepository.findFirstByBusIdAndAssignmentStatusOrderByStartedAtDesc(
                busId,
                AssignmentStatus.ACTIVE
        );
    }

    private DriverProfile resolveDriverProfile(Long driverProfileId) {
        if (driverProfileId == null) {
            return null;
        }

        DriverProfile driverProfile = driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (driverProfile.getUser() == null || Boolean.FALSE.equals(driverProfile.getUser().getIsActive())) {
            throw new BadRequestException("Selected driver is inactive");
        }

        return driverProfile;
    }

    private String resolveOperatorName(BusDto dto, DriverProfile driverProfile) {
        if (driverProfile != null && driverProfile.getUser() != null) {
            return driverProfile.getUser().getFullName();
        }
        return dto.getOperatorName();
    }

    private void syncAssignment(Bus bus, BusDto dto, DriverProfile driverProfile, BusAssignment activeAssignment) {
        if (driverProfile == null) {
            return;
        }

        RouteVariant routeVariant = resolveRouteVariant(dto, driverProfile, activeAssignment);

        if (activeAssignment != null) {
            activeAssignment.setDriverProfile(driverProfile);
            activeAssignment.setRouteVariant(routeVariant);
            busAssignmentRepository.save(activeAssignment);
            return;
        }

        BusAssignment assignment = new BusAssignment();
        assignment.setBus(bus);
        assignment.setDriverProfile(driverProfile);
        assignment.setRouteVariant(routeVariant);
        assignment.setAssignmentStatus(AssignmentStatus.ACTIVE);
        busAssignmentRepository.save(assignment);
    }

    private RouteVariant resolveRouteVariant(BusDto dto, DriverProfile driverProfile, BusAssignment activeAssignment) {
        if (dto.getRouteVariantId() != null) {
            return routeVariantRepository.findById(dto.getRouteVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Route Variant not found"));
        }

        if (activeAssignment != null && activeAssignment.getRouteVariant() != null) {
            return activeAssignment.getRouteVariant();
        }

        BusAssignment driverAssignment = busAssignmentRepository
                .findFirstByDriverProfileIdAndAssignmentStatusOrderByStartedAtDesc(
                        driverProfile.getId(),
                        AssignmentStatus.ACTIVE
                )
                .orElse(null);

        if (driverAssignment != null && driverAssignment.getRouteVariant() != null) {
            return driverAssignment.getRouteVariant();
        }

        return routeVariantRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No route variants available. Please create a route first."));
    }

    private String buildRouteVariantLabel(RouteVariant routeVariant) {
        if (routeVariant.getRoute() != null) {
            return routeVariant.getRoute().getRouteNumber() + " - " +
                    routeVariant.getOriginName() + " to " + routeVariant.getDestinationName();
        }
        return routeVariant.getOriginName() + " to " + routeVariant.getDestinationName();
    }
}
