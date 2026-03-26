package com.transitshield.backend.controller;

import com.transitshield.backend.dto.ComplaintDto;
import com.transitshield.backend.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final ComplaintService complaintService;

    @GetMapping
    public ResponseEntity<List<ComplaintDto>> getAll() {
        return ResponseEntity.ok(complaintService.findAll());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ComplaintDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(complaintService.updateStatus(id, body.getOrDefault("status", "OPEN")));
    }
}
