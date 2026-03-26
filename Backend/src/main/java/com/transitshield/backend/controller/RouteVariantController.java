package com.transitshield.backend.controller;

import com.transitshield.backend.dto.RouteVariantDto;
import com.transitshield.backend.service.RouteVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route-variants")
@RequiredArgsConstructor
public class RouteVariantController {
    private final RouteVariantService routeVariantService;

    @GetMapping
    public List<RouteVariantDto> getAll() {
        return routeVariantService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteVariantDto> getById(@PathVariable Long id) {
        RouteVariantDto dto = routeVariantService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public RouteVariantDto create(@RequestBody RouteVariantDto dto) {
        return routeVariantService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteVariantDto> update(@PathVariable Long id, @RequestBody RouteVariantDto dto) {
        RouteVariantDto updated = routeVariantService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeVariantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
