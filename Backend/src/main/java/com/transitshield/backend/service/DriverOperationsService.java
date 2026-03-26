package com.transitshield.backend.service;

import com.transitshield.backend.dto.AlertDto;
import com.transitshield.backend.dto.BusQrCodeDto;
import com.transitshield.backend.dto.DriverDashboardDto;
import com.transitshield.backend.dto.DriverScheduleDto;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.BusLocation;
import com.transitshield.backend.entity.BusQrCode;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.PassengerTrip;
import com.transitshield.backend.entity.RouteVariant;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusLocationRepository;
import com.transitshield.backend.repository.BusQrCodeRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.PassengerTripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverOperationsService {

    private final DriverProfileRepository driverProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final BusQrCodeRepository busQrCodeRepository;
    private final PassengerTripRepository passengerTripRepository;
    private final BusLocationRepository busLocationRepository;

    public DriverDashboardDto getDashboard(User user) {
        DriverProfile driverProfile = getDriverProfile(user);
        BusAssignment activeAssignment = getActiveAssignment(driverProfile.getId());
        BusQrCode activeQr = activeAssignment != null
                ? busQrCodeRepository.findByBusIdAndIsActiveTrue(activeAssignment.getBus().getId()).orElse(null)
                : null;

        DriverDashboardDto dto = new DriverDashboardDto();
        dto.setName(user.getFullName());
        dto.setId(user.getId());
        dto.setDriverProfileId(driverProfile.getId());
        dto.setActiveAssignmentId(activeAssignment != null ? activeAssignment.getId() : null);
        dto.setActiveBusId(activeAssignment != null && activeAssignment.getBus() != null
                ? activeAssignment.getBus().getId()
                : null);
        dto.setProfileInitial(buildInitial(user.getFullName()));
        dto.setDepot(driverProfile.getDepot());
        dto.setIsOnline(activeAssignment != null);
        dto.setDemerits(driverProfile.getDemeritPoints() != null ? driverProfile.getDemeritPoints() : 0);
        dto.setMaxDemerits(20);
        dto.setCurrentRoute(buildCurrentRoute(activeAssignment));
        dto.setTripsToday((int) countTripsToday(driverProfile.getId()));
        dto.setOnTimePercentage(calculateOnTimePercentage(driverProfile.getId()));
        dto.setComplaintsToday(0);

        dto.setAlerts(buildDashboardAlerts(driverProfile, activeAssignment, activeQr));
        dto.setLostItems(List.of());

        return dto;
    }

    public DriverScheduleDto getSchedule(User user) {
        DriverProfile driverProfile = getDriverProfile(user);
        BusAssignment activeAssignment = getActiveAssignment(driverProfile.getId());

        if (activeAssignment == null) {
            DriverScheduleDto dto = new DriverScheduleDto();
            dto.setDriverProfileId(driverProfile.getId());
            dto.setDriverUserId(user.getId());
            dto.setDriverName(user.getFullName());
            dto.setDriverCode(driverProfile.getDriverCode());
            dto.setDepot(driverProfile.getDepot());
            dto.setAssignmentStatus(AssignmentStatus.CANCELLED.name());
            dto.setHasActiveQr(false);
            return dto;
        }

        BusQrCode activeQr = busQrCodeRepository.findByBusIdAndIsActiveTrue(activeAssignment.getBus().getId()).orElse(null);
        return mapSchedule(activeAssignment, activeQr);
    }

    public List<AlertDto> getAlerts(User user) {
        DriverProfile driverProfile = getDriverProfile(user);
        BusAssignment activeAssignment = getActiveAssignment(driverProfile.getId());
        BusQrCode activeQr = activeAssignment != null
                ? busQrCodeRepository.findByBusIdAndIsActiveTrue(activeAssignment.getBus().getId()).orElse(null)
                : null;

        List<AlertDto> alerts = new ArrayList<>();

        if (activeAssignment == null) {
            alerts.add(alert(
                    "SYSTEM",
                    "NO_ASSIGNMENT",
                    "No active assignment",
                    "You do not currently have an active bus assignment. Contact admin for scheduling.",
                    "Now"
            ));
            return alerts;
        }

        if (activeQr == null) {
            alerts.add(alert(
                    "SYSTEM",
                    "MISSING_QR",
                    "Bus QR not generated",
                    "Your assigned bus does not have an active admin-generated QR yet.",
                    "Now"
            ));
        }

        Integer demerits = driverProfile.getDemeritPoints() != null ? driverProfile.getDemeritPoints() : 0;
        if (demerits > 0) {
            alerts.add(alert(
                    "SYSTEM",
                    "DEMERITS",
                    "Demerit reminder",
                    "Current demerit count: " + demerits + ". Drive carefully and maintain schedule discipline.",
                    "Today"
            ));
        }

        BusLocation latestLocation = getLatestDriverLocation(driverProfile.getId());
        if (latestLocation == null) {
            alerts.add(alert(
                    "SYSTEM",
                    "LOCATION_MISSING",
                    "Location updates missing",
                    "No recent location updates were found for your assigned bus.",
                    "Today"
            ));
        } else if (latestLocation.getRecordedAt() != null
                && latestLocation.getRecordedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
            alerts.add(alert(
                    "SYSTEM",
                    "LOCATION_STALE",
                    "Location update is stale",
                    "Your latest bus location update is older than 15 minutes.",
                    timeAgo(latestLocation.getRecordedAt())
            ));
        }

        long activeTrips = passengerTripRepository
                .findByBusAssignmentIdAndTripStatus(activeAssignment.getId(), TripStatus.ACTIVE)
                .size();
        if (activeTrips == 0) {
            alerts.add(alert(
                    "SYSTEM",
                    "NO_ACTIVE_PASSENGERS",
                    "No active passengers",
                    "There are currently no validated active passenger trips on this assignment.",
                    "Today"
            ));
        }

        return alerts;
    }

    public BusQrCodeDto getAssignedBusQr(User user) {
        DriverProfile driverProfile = getDriverProfile(user);
        BusAssignment activeAssignment = getActiveAssignment(driverProfile.getId());

        if (activeAssignment == null) {
            return null;
        }

        return busQrCodeRepository.findByBusIdAndIsActiveTrue(activeAssignment.getBus().getId())
                .map(this::mapQr)
                .orElse(null);
    }

    private DriverProfile getDriverProfile(User user) {
        return driverProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for user ID: " + user.getId()));
    }

    private BusAssignment getActiveAssignment(Long driverProfileId) {
        return busAssignmentRepository
                .findFirstByDriverProfileIdAndAssignmentStatusOrderByStartedAtDesc(driverProfileId, AssignmentStatus.ACTIVE)
                .orElse(null);
    }

    private DriverScheduleDto mapSchedule(BusAssignment assignment, BusQrCode activeQr) {
        DriverScheduleDto dto = new DriverScheduleDto();

        dto.setAssignmentId(assignment.getId());
        dto.setAssignmentStatus(assignment.getAssignmentStatus().name());
        dto.setStartedAt(assignment.getStartedAt());
        dto.setEndedAt(assignment.getEndedAt());

        DriverProfile driverProfile = assignment.getDriverProfile();
        if (driverProfile != null) {
            dto.setDriverProfileId(driverProfile.getId());
            dto.setDriverCode(driverProfile.getDriverCode());
            dto.setDepot(driverProfile.getDepot());

            User user = driverProfile.getUser();
            if (user != null) {
                dto.setDriverUserId(user.getId());
                dto.setDriverName(user.getFullName());
            }
        }

        if (assignment.getBus() != null) {
            dto.setBusId(assignment.getBus().getId());
            dto.setBusCode(assignment.getBus().getBusCode());
            dto.setBusDisplayName(assignment.getBus().getBusDisplayName());
            dto.setRegistrationNumber(assignment.getBus().getRegistrationNumber());
            dto.setCapacity(assignment.getBus().getCapacity());
            dto.setOperatorName(assignment.getBus().getOperatorName());
            dto.setBusStatus(assignment.getBus().getStatus() != null ? assignment.getBus().getStatus().name() : null);
        }

        RouteVariant routeVariant = assignment.getRouteVariant();
        if (routeVariant != null) {
            dto.setRouteVariantId(routeVariant.getId());
            dto.setRouteVariantCode(routeVariant.getVariantCode());
            dto.setOriginName(routeVariant.getOriginName());
            dto.setDestinationName(routeVariant.getDestinationName());
            dto.setDirectionLabel(routeVariant.getDirectionLabel());

            if (routeVariant.getRoute() != null) {
                dto.setRouteNumber(routeVariant.getRoute().getRouteNumber());
                dto.setRouteName(routeVariant.getRoute().getDisplayName());
            }
        }

        dto.setHasActiveQr(activeQr != null);
        if (activeQr != null) {
            dto.setActiveQrId(activeQr.getId());
            dto.setActiveQrLabel(activeQr.getQrLabel());
            dto.setActiveQrToken(activeQr.getQrToken());
        }

        return dto;
    }

    private DriverDashboardDto.DriverAlertDto toDashboardAlert(AlertDto alert) {
        DriverDashboardDto.DriverAlertDto dto = new DriverDashboardDto.DriverAlertDto();
        dto.setType(alert.getType());
        dto.setTitle(alert.getTitle());
        dto.setMessage(alert.getMessage());
        dto.setTimestamp(alert.getTimestamp());
        return dto;
    }

    private List<DriverDashboardDto.DriverAlertDto> buildDashboardAlerts(
            DriverProfile driverProfile,
            BusAssignment activeAssignment,
            BusQrCode activeQr
    ) {
        List<DriverDashboardDto.DriverAlertDto> alerts = new ArrayList<>();
        getAlerts(driverProfile.getUser()).stream()
                .map(this::toDashboardAlert)
                .forEach(alerts::add);

        if (alerts.isEmpty()) {
            DriverDashboardDto.DriverAlertDto ok = new DriverDashboardDto.DriverAlertDto();
            ok.setType("SYSTEM");
            ok.setTitle("All clear");
            ok.setMessage(activeAssignment != null && activeQr != null
                    ? "Your schedule, assigned bus, and QR are ready for service."
                    : "No urgent driver alerts at the moment.");
            ok.setTimestamp("Now");
            alerts.add(ok);
        }

        return alerts;
    }

    private AlertDto alert(String type, String id, String title, String message, String timestamp) {
        AlertDto dto = new AlertDto();
        dto.setType(type);
        dto.setId(id);
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setTimestamp(timestamp);
        return dto;
    }

    private long countTripsToday(Long driverProfileId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return passengerTripRepository.countByBusAssignmentDriverProfileIdAndCreatedAtBetween(driverProfileId, start, end);
    }

    private int calculateOnTimePercentage(Long driverProfileId) {
        BusLocation latestLocation = getLatestDriverLocation(driverProfileId);
        if (latestLocation == null || latestLocation.getRecordedAt() == null) {
            return 85;
        }

        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(10);
        return latestLocation.getRecordedAt().isAfter(staleThreshold) ? 95 : 75;
    }

    private BusLocation getLatestDriverLocation(Long driverProfileId) {
        return busLocationRepository.findByDriverProfileIdOrderByRecordedAtDesc(driverProfileId).stream()
                .max(Comparator.comparing(BusLocation::getRecordedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private String buildCurrentRoute(BusAssignment assignment) {
        if (assignment == null || assignment.getRouteVariant() == null) {
            return "No active assignment";
        }

        RouteVariant routeVariant = assignment.getRouteVariant();
        String routeNumber = routeVariant.getRoute() != null ? routeVariant.getRoute().getRouteNumber() : "";
        String routeName = routeVariant.getRoute() != null ? routeVariant.getRoute().getDisplayName() : routeVariant.getVariantCode();

        if (routeNumber == null || routeNumber.isBlank()) {
            return routeName;
        }

        return "Route " + routeNumber + " - " + routeName;
    }

    private String buildInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "DR";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private String timeAgo(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = Math.max(1, duration.toMinutes());

        if (minutes < 60) {
            return minutes + " min ago";
        }

        long hours = Math.max(1, duration.toHours());
        return hours + " hr ago";
    }

    private BusQrCodeDto mapQr(BusQrCode qr) {
        BusQrCodeDto dto = new BusQrCodeDto();
        dto.setId(qr.getId());
        dto.setBusId(qr.getBus() != null ? qr.getBus().getId() : null);
        dto.setQrToken(qr.getQrToken());
        dto.setQrLabel(qr.getQrLabel());
        dto.setIsActive(qr.getIsActive());
        return dto;
    }
}
