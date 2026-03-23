package com.transitshield.backend.controller;

import com.transitshield.backend.dto.LostItemReportDto;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.LostItemReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverOperationController {

    private final LostItemReportService lostItemReportService;

    @GetMapping("/lost-items")
    public ResponseEntity<List<LostItemReportDto>> getDriverLostItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lostItemReportService.getForDriverAssignedBus(user));
    }
}
