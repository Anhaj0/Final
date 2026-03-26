package com.transitshield.backend.controller;

import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.service.LostItemReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemReportService lostItemReportService;

    @PostMapping
    public ResponseEntity<LostItemReportDto> create(@RequestBody LostItemReportDto dto) {
        return ResponseEntity.ok(lostItemReportService.create(dto));
    }
}
