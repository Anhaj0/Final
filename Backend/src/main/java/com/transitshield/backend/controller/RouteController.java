package com.transitshield.backend.controller;

import com.transitshield.backend.dto.RouteDto;
import com.transitshield.backend.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;

    @GetMapping
    public List<RouteDto> getAll() {
        return routeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteDto> getById(@PathVariable("id") Long id) {
        RouteDto dto = routeService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public RouteDto create(@RequestBody RouteDto dto) {
        return routeService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteDto> update(@PathVariable("id") Long id, @RequestBody RouteDto dto) {
        RouteDto updated = routeService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        routeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
