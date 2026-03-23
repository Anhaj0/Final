package com.transitshield.backend.controller;

import com.transitshield.backend.dto.PassengerTaskCapacityRequest;
import com.transitshield.backend.dto.PassengerTaskLocationRequest;
import com.transitshield.backend.dto.PassengerTaskResponse;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.PassengerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class PassengerTaskController {

    private final PassengerTaskService passengerTaskService;

    @PostMapping("/location")
    public ResponseEntity<PassengerTaskResponse> submitLocationTask(
            @AuthenticationPrincipal User user,
            @RequestBody PassengerTaskLocationRequest request
    ) {
        return ResponseEntity.ok(passengerTaskService.reportLocation(user, request));
    }

    @PostMapping("/capacity")
    public ResponseEntity<PassengerTaskResponse> submitCapacityTask(
            @AuthenticationPrincipal User user,
            @RequestBody PassengerTaskCapacityRequest request
    ) {
        return ResponseEntity.ok(passengerTaskService.reportCapacity(user, request));
    }
}
