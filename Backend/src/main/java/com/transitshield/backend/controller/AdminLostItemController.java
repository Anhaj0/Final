package com.transitshield.backend.controller;

import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.service.LostItemReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/lost-items")
@RequiredArgsConstructor
public class AdminLostItemController {

    private final LostItemReportService lostItemReportService;

    @GetMapping
    public ResponseEntity<List<LostItemReportDto>> getAll() {
        return ResponseEntity.ok(lostItemReportService.findAll());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LostItemReportDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(lostItemReportService.updateStatus(id, body.getOrDefault("status", "OPEN")));
    }
}
