package com.transitshield.backend.controller;

import com.transitshield.backend.dto.ComplaintDto;
import com.transitshield.backend.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ComplaintDto> create(@RequestBody ComplaintDto dto) {
        return ResponseEntity.ok(complaintService.create(dto));
    }
}
