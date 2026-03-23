package com.transitshield.backend.controller;

import com.transitshield.backend.dto.StopDto;
import com.transitshield.backend.service.StopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class StopController {
    private final StopService stopService;

    @GetMapping
    public List<StopDto> getAll() {
        return stopService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StopDto> getById(@PathVariable("id") Long id) {
        StopDto dto = stopService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public StopDto create(@RequestBody StopDto dto) {
        return stopService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StopDto> update(@PathVariable("id") Long id, @RequestBody StopDto dto) {
        StopDto updated = stopService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        stopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
