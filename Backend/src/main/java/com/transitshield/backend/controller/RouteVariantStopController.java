package com.transitshield.backend.controller;

import com.transitshield.backend.dto.RouteVariantStopDto;
import com.transitshield.backend.service.RouteVariantStopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route-variant-stops")
@RequiredArgsConstructor
public class RouteVariantStopController {
    private final RouteVariantStopService routeVariantStopService;

    @GetMapping
    public List<RouteVariantStopDto> getAll() {
        return routeVariantStopService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteVariantStopDto> getById(@PathVariable Long id) {
        RouteVariantStopDto dto = routeVariantStopService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public RouteVariantStopDto create(@RequestBody RouteVariantStopDto dto) {
        return routeVariantStopService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteVariantStopDto> update(@PathVariable Long id, @RequestBody RouteVariantStopDto dto) {
        RouteVariantStopDto updated = routeVariantStopService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeVariantStopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
