package com.transitshield.backend.controller;

import com.transitshield.backend.dto.*;
import com.transitshield.backend.service.PassengerTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PassengerTripDto> startTrip(@RequestBody TripStartRequest request) {
        return ResponseEntity.ok(tripService.startTrip(request));
    }

    @GetMapping("/passenger/{passengerId}/balance")
    public ResponseEntity<PassengerBalanceDto> getPassengerBalance(@PathVariable Long passengerId) {
        return ResponseEntity.ok(tripService.getPassengerBalance(passengerId));
    }

    @PostMapping("/extend")
    public ResponseEntity<PassengerTripDto> extendTrip(@RequestBody TripExtendRequest request) {
        return ResponseEntity.ok(tripService.extendTrip(request));
    }

    @PostMapping("/end")
    public ResponseEntity<PassengerTripDto> endTrip(@RequestBody TripEndRequest request) {
        return ResponseEntity.ok(tripService.endTrip(request));
    }

    @GetMapping("/passenger/{passengerId}/active")
    public ResponseEntity<PassengerTripDto> getActiveTrip(@PathVariable Long passengerId) {
        return ResponseEntity.ok(tripService.getActiveTrip(passengerId));
    }

    @GetMapping("/passenger/{passengerId}/history")
    public ResponseEntity<List<PassengerTripDto>> getTripHistory(@PathVariable Long passengerId) {
        return ResponseEntity.ok(tripService.getTripHistory(passengerId));
    }
}
