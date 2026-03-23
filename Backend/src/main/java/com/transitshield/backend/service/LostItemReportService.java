package com.transitshield.backend.service;

import com.transitshield.backend.dto.LostItemReportCreateRequest;
import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.dto.LostItemStatusUpdateRequest;
import com.transitshield.backend.entity.Bus;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.LostItemReport;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.entity.enums.LostItemStatus;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.LostItemReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemReportService {

    private final LostItemReportRepository lostItemReportRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;

    @Transactional
    public LostItemReportDto create(User reporter, LostItemReportCreateRequest request) {
        if (request == null || isBlank(request.getItemTitle()) || isBlank(request.getDescription())) {
            throw new BadRequestException("Item title and description are required");
        }

        LostItemReport entity = new LostItemReport();
        entity.setReporterUser(reporter);
        entity.setItemTitle(request.getItemTitle().trim());
        entity.setDescription(request.getDescription().trim());
        entity.setCategory(trimToNull(request.getCategory()));
        entity.setRouteInfo(trimToNull(request.getRouteInfo()));
        entity.setBusInfo(trimToNull(request.getBusInfo()));
        entity.setContactDetails(trimToNull(request.getContactDetails()));
        entity.setLostAt(parseDateTimeOrNull(request.getLostAt()));
        entity.setStatus(LostItemStatus.REPORTED);

        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return toDto(lostItemReportRepository.save(entity));
    }

    public List<LostItemReportDto> getMine(User user) {
        return lostItemReportRepository.findByReporterUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toDto).toList();
    }

    public List<LostItemReportDto> getAll(LostItemStatus status) {
        List<LostItemReport> items = status == null
                ? lostItemReportRepository.findAll().stream().sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).toList()
                : lostItemReportRepository.findByStatusOrderByCreatedAtDesc(status);
        return items.stream().map(this::toDto).toList();
    }

    public List<LostItemReportDto> getForDriverAssignedBus(User driverUser) {
        DriverProfile driverProfile = driverProfileRepository.findByUserId(driverUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        BusAssignment assignment = busAssignmentRepository
                .findFirstByDriverProfileIdAndAssignmentStatusOrderByStartedAtDesc(
                        driverProfile.getId(),
                        AssignmentStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("No active assignment found for driver"));

        Bus bus = assignment.getBus();
        if (bus == null) {
            return List.of();
        }

        String busCode = bus.getBusCode() != null ? bus.getBusCode().toLowerCase() : "";
        String registration = bus.getRegistrationNumber() != null ? bus.getRegistrationNumber().toLowerCase() : "";
        String displayName = bus.getBusDisplayName() != null ? bus.getBusDisplayName().toLowerCase() : "";

        return lostItemReportRepository.findAll().stream()
                .filter(report -> {
                    String busInfo = report.getBusInfo() != null ? report.getBusInfo().toLowerCase() : "";
                    return (!busCode.isBlank() && busInfo.contains(busCode))
                            || (!registration.isBlank() && busInfo.contains(registration))
                            || (!displayName.isBlank() && busInfo.contains(displayName));
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public LostItemReportDto updateStatus(Long reportId, LostItemStatusUpdateRequest request) {
        LostItemReport report = lostItemReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item report not found: " + reportId));

        if (request == null || request.getStatus() == null) {
            throw new BadRequestException("Status is required");
        }

        report.setStatus(request.getStatus());
        report.setAdminNotes(trimToNull(request.getAdminNotes()));
        report.setResolutionNotes(trimToNull(request.getResolutionNotes()));
        report.setUpdatedAt(LocalDateTime.now());

        return toDto(lostItemReportRepository.save(report));
    }

    private LostItemReportDto toDto(LostItemReport e) {
        LostItemReportDto dto = new LostItemReportDto();
        dto.setId(e.getId());
        dto.setReporterUserId(e.getReporterUser() != null ? e.getReporterUser().getId() : null);
        dto.setReporterName(e.getReporterUser() != null ? e.getReporterUser().getFullName() : null);
        dto.setReporterPhoneNumber(e.getReporterUser() != null ? e.getReporterUser().getPhoneNumber() : null);
        dto.setReporterRole(e.getReporterUser() != null && e.getReporterUser().getRole() != null ? e.getReporterUser().getRole().name() : null);
        dto.setItemTitle(e.getItemTitle());
        dto.setDescription(e.getDescription());
        dto.setCategory(e.getCategory());
        dto.setRouteInfo(e.getRouteInfo());
        dto.setBusInfo(e.getBusInfo());
        dto.setLostAt(e.getLostAt() != null ? e.getLostAt().toString() : null);
        dto.setContactDetails(e.getContactDetails());
        dto.setStatus(e.getStatus());
        dto.setAdminNotes(e.getAdminNotes());
        dto.setResolutionNotes(e.getResolutionNotes());
        dto.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
        dto.setUpdatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt().toString() : null);
        return dto;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private LocalDateTime parseDateTimeOrNull(String value) {
        if (isBlank(value)) return null;
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("lostAt must be an ISO date-time (e.g., 2026-03-23T10:30:00)");
        }
    }
}
