package com.transitshield.backend.controller;

import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.dto.LostItemStatusUpdateRequest;
import com.transitshield.backend.entity.enums.LostItemStatus;
import com.transitshield.backend.service.LostItemReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lost-items")
@RequiredArgsConstructor
public class AdminLostItemReportController {

    private final LostItemReportService lostItemReportService;

    @GetMapping
    public ResponseEntity<List<LostItemReportDto>> getAll(@RequestParam(value = "status", required = false) LostItemStatus status) {
        return ResponseEntity.ok(lostItemReportService.getAll(status));
    }

    @PutMapping("/{reportId}/status")
    public ResponseEntity<LostItemReportDto> updateStatus(
            @PathVariable("reportId") Long reportId,
            @RequestBody LostItemStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(lostItemReportService.updateStatus(reportId, request));
    }
}
