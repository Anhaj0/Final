package com.transitshield.backend.service;

import com.transitshield.backend.dto.ComplaintDto;
import com.transitshield.backend.entity.Complaint;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public List<ComplaintDto> findAll() {
        return complaintRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ComplaintDto create(ComplaintDto dto) {
        Complaint complaint = new Complaint();
        complaint.setPassengerName(dto.getPassengerName());
        complaint.setPassengerContact(dto.getPassengerContact());
        complaint.setBusReference(dto.getBusReference());
        complaint.setSubject(dto.getSubject());
        complaint.setDescription(dto.getDescription());
        complaint.setStatus(dto.getStatus() != null ? dto.getStatus() : "OPEN");
        complaint.setCreatedAt(LocalDateTime.now());
        return mapToDto(complaintRepository.save(complaint));
    }

    public ComplaintDto updateStatus(Long id, String status) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        complaint.setStatus(status);
        return mapToDto(complaintRepository.save(complaint));
    }

    private ComplaintDto mapToDto(Complaint complaint) {
        ComplaintDto dto = new ComplaintDto();
        dto.setId(complaint.getId());
        dto.setPassengerName(complaint.getPassengerName());
        dto.setPassengerContact(complaint.getPassengerContact());
        dto.setBusReference(complaint.getBusReference());
        dto.setSubject(complaint.getSubject());
        dto.setDescription(complaint.getDescription());
        dto.setStatus(complaint.getStatus());
        dto.setCreatedAt(complaint.getCreatedAt());
        return dto;
    }
}
