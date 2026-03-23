package com.transitshield.backend.controller;

import com.transitshield.backend.dto.FarePreviewRequest;
import com.transitshield.backend.dto.PassengerTripDto;
import com.transitshield.backend.dto.TripEndRequest;
import com.transitshield.backend.dto.TripExtendRequest;
import com.transitshield.backend.dto.TripStartRequest;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.PassengerTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class PassengerTripController {

    private final PassengerTripService tripService;

    @PostMapping("/preview-fare")
    public ResponseEntity<Double> previewFare(@RequestBody FarePreviewRequest request) {
        return ResponseEntity.ok(tripService.previewFare(request));
    }

    @PostMapping("/start")
    public ResponseEntity<PassengerTripDto> startTrip(
            @AuthenticationPrincipal User user,
            @RequestBody TripStartRequest request
    ) {
        return ResponseEntity.ok(tripService.startTrip(user, request));
    }

    @PostMapping("/extend")
    public ResponseEntity<PassengerTripDto> extendTrip(@RequestBody TripExtendRequest request) {
        return ResponseEntity.ok(tripService.extendTrip(request));
    }

    @PostMapping("/end")
    public ResponseEntity<PassengerTripDto> endTrip(
            @AuthenticationPrincipal User user,
            @RequestBody TripEndRequest request
    ) {
        return ResponseEntity.ok(tripService.endTrip(user, request));
    }

    @GetMapping("/me/active")
    public ResponseEntity<PassengerTripDto> getMyActiveTrip(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(tripService.getActiveTripForUser(user));
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<PassengerTripDto>> getMyTripHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(tripService.getTripHistoryForUser(user));
    }

    @GetMapping("/passenger/{passengerId}/active")
    public ResponseEntity<PassengerTripDto> getActiveTrip(@PathVariable("passengerId") Long passengerId) {
        return ResponseEntity.ok(tripService.getActiveTrip(passengerId));
    }

    @GetMapping("/passenger/{passengerId}/history")
    public ResponseEntity<List<PassengerTripDto>> getTripHistory(@PathVariable("passengerId") Long passengerId) {
        return ResponseEntity.ok(tripService.getTripHistory(passengerId));
    }
}
