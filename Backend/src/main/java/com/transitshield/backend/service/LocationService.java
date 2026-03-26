package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusLocationDto;
import com.transitshield.backend.dto.LocationUpdateRequest;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.BusLocation;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusLocationRepository;
import com.transitshield.backend.repository.BusRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final BusLocationRepository locationRepository;
    private final BusRepository busRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;

    public void updateLocation(LocationUpdateRequest request) {
        DriverProfile driver = driverProfileRepository.findById(request.getDriverProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        BusAssignment activeAssignment = busAssignmentRepository
                .findFirstByDriverProfileIdAndAssignmentStatusOrderByStartedAtDesc(
                        driver.getId(),
                        AssignmentStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("No active assignment found for driver"));
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        if (activeAssignment.getBus() == null || !activeAssignment.getBus().getId().equals(bus.getId())) {
            throw new ResourceNotFoundException("Driver is not assigned to this bus");
        }

        BusLocation location = new BusLocation();
        location.setBus(bus);
        location.setDriverProfile(driver);
        location.setRouteVariant(activeAssignment.getRouteVariant());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setSpeedKmh(request.getSpeedKmh());
        location.setHeading(request.getHeading());
        location.setOccupancyStatus(request.getOccupancyStatus());
        location.setSourceType(request.getSourceType());
        location.setRecordedAt(LocalDateTime.now());
        
        locationRepository.save(location);
    }

    public List<BusLocationDto> getLiveLocations() {
        return locationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BusLocationDto getBusLocation(Long busId) {
        return locationRepository.findTopByBusIdOrderByRecordedAtDesc(busId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("No location found for this bus"));
    }

    public List<BusLocationDto> getRouteVariantLocations(Long routeVariantId) {
        return locationRepository.findAll().stream()
                .filter(l -> l.getRouteVariant() != null && l.getRouteVariant().getId().equals(routeVariantId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BusLocationDto mapToDto(BusLocation entity) {
        BusLocationDto dto = new BusLocationDto();
        dto.setId(entity.getId());
        dto.setBusId(entity.getBus().getId());
        if (entity.getRouteVariant() != null) dto.setRouteVariantId(entity.getRouteVariant().getId());
        if (entity.getDriverProfile() != null) dto.setDriverProfileId(entity.getDriverProfile().getId());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setSpeedKmh(entity.getSpeedKmh());
        dto.setHeading(entity.getHeading());
        dto.setOccupancyStatus(entity.getOccupancyStatus());
        dto.setRecordedAt(entity.getRecordedAt());
        dto.setSourceType(entity.getSourceType());
        return dto;
    }
}
