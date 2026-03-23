package com.transitshield.backend.controller;

import com.transitshield.backend.dto.BusLocationDto;
import com.transitshield.backend.dto.LocationUpdateRequest;
import com.transitshield.backend.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/update")
    public ResponseEntity<Void> updateLocation(@RequestBody LocationUpdateRequest request) {
        locationService.updateLocation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/live")
    public ResponseEntity<List<BusLocationDto>> getLiveLocations() {
        return ResponseEntity.ok(locationService.getLiveLocations());
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<BusLocationDto> getBusLocation(@PathVariable("busId") Long busId) {
        return ResponseEntity.ok(locationService.getBusLocation(busId));
    }

    @GetMapping("/route-variant/{routeVariantId}")
    public ResponseEntity<List<BusLocationDto>> getRouteVariantLocations(@PathVariable("routeVariantId") Long routeVariantId) {
        return ResponseEntity.ok(locationService.getRouteVariantLocations(routeVariantId));
    }
}
