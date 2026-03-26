package com.transitshield.backend.controller;

import com.transitshield.backend.dto.QrScanRequest;
import com.transitshield.backend.dto.QrScanResponse;
import com.transitshield.backend.dto.PassengerTripDto;
import com.transitshield.backend.service.QrFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrFlowController {

    private final QrFlowService qrFlowService;

    @PostMapping("/scan")
    public ResponseEntity<QrScanResponse> scanQr(@RequestBody QrScanRequest request) {
        return ResponseEntity.ok(qrFlowService.scanQr(request));
    }

    @PostMapping("/verify-ticket/{tripRef}")
    public ResponseEntity<PassengerTripDto> verifyTicket(@PathVariable String tripRef) {
        return ResponseEntity.ok(qrFlowService.verifyTicket(tripRef));
    }
}
