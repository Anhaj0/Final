package com.transitshield.backend.controller;

import com.transitshield.backend.dto.BusDto;
import com.transitshield.backend.dto.BusQrCodeDto;
import com.transitshield.backend.service.BusService;
import com.transitshield.backend.service.QrFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {
    private final BusService busService;
    private final QrFlowService qrFlowService;

    @GetMapping
    public List<BusDto> getAll() {
        return busService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusDto> getById(@PathVariable Long id) {
        BusDto dto = busService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public BusDto create(@RequestBody BusDto dto) {
        return busService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusDto> update(@PathVariable Long id, @RequestBody BusDto dto) {
        BusDto updated = busService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        busService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the currently active QR code for a specific bus.
     * Returns null/empty if admin has not generated a QR for this bus yet.
     */
    @GetMapping("/{busId}/active-qr")
    public ResponseEntity<BusQrCodeDto> getActiveQr(@PathVariable Long busId) {
        BusQrCodeDto dto = qrFlowService.getActiveQrForBus(busId);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.noContent().build();
    }
}
