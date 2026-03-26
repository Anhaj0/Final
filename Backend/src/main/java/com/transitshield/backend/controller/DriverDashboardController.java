package com.transitshield.backend.controller;

import com.transitshield.backend.dto.DriverDashboardDto;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.DriverOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverDashboardController {

    private final DriverOperationsService driverOperationsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DriverDashboardDto> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(driverOperationsService.getDashboard(user));
    }
}
