package com.transitshield.backend.service;

import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.entity.LostItemReport;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.LostItemReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LostItemReportService {

    private final LostItemReportRepository lostItemReportRepository;

    public List<LostItemReportDto> findAll() {
        return lostItemReportRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public LostItemReportDto create(LostItemReportDto dto) {
        LostItemReport report = new LostItemReport();
        report.setItemName(dto.getItemName());
        report.setReporterName(dto.getReporterName());
        report.setReporterContact(dto.getReporterContact());
        report.setRouteOrBus(dto.getRouteOrBus());
        report.setNotes(dto.getNotes());
        report.setStatus(dto.getStatus() != null ? dto.getStatus() : "OPEN");
        report.setCreatedAt(LocalDateTime.now());
        return mapToDto(lostItemReportRepository.save(report));
    }

    public LostItemReportDto updateStatus(Long id, String status) {
        LostItemReport report = lostItemReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item report not found"));
        report.setStatus(status);
        return mapToDto(lostItemReportRepository.save(report));
    }

    private LostItemReportDto mapToDto(LostItemReport report) {
        LostItemReportDto dto = new LostItemReportDto();
        dto.setId(report.getId());
        dto.setItemName(report.getItemName());
        dto.setReporterName(report.getReporterName());
        dto.setReporterContact(report.getReporterContact());
        dto.setRouteOrBus(report.getRouteOrBus());
        dto.setNotes(report.getNotes());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
}
