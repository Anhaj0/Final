package com.transitshield.backend.controller;

import com.transitshield.backend.dto.BusQrCodeDto;
import com.transitshield.backend.dto.auth.AuthResponse;
import com.transitshield.backend.dto.auth.RegisterRequest;
import com.transitshield.backend.dto.RewardTransactionDto;
import com.transitshield.backend.dto.PassengerBalanceDto;
import com.transitshield.backend.service.RewardService;
import com.transitshield.backend.service.AuthService;
import com.transitshield.backend.service.QrFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final QrFlowService qrFlowService;
    private final RewardService rewardService;

    /**
     * Admin creates a new driver account.
     * Driver cannot self-register.
     */
    @PostMapping("/drivers")
    public ResponseEntity<AuthResponse> createDriver(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerDriver(request));
    }

    /**
     * Admin generates a QR code for a specific bus.
     * Any existing active QR for that bus gets deactivated first.
     * Only one active QR per bus at any time.
     */
    @PostMapping("/buses/{busId}/generate-qr")
    public ResponseEntity<BusQrCodeDto> generateQr(@PathVariable Long busId) {
        return ResponseEntity.ok(qrFlowService.generateQrForBus(busId));
    }

    // --- Admin Reward endpoints ---
    @GetMapping("/rewards/balances")
    public ResponseEntity<List<PassengerBalanceDto>> getPassengerBalances() {
        List<PassengerBalanceDto> dtos = rewardService.getAllBalances().stream().map(p -> {
            PassengerBalanceDto dto = new PassengerBalanceDto();
            dto.setFullName(p.getUser().getFullName());
            dto.setPublicUserId(p.getPublicUserId());
            dto.setTotalPoints(p.getTotalPoints());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/rewards/transactions")
    public ResponseEntity<List<RewardTransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(rewardService.getAllTransactions());
    }
}
