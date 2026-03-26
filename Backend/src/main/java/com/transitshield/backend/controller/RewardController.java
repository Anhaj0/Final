package com.transitshield.backend.controller;

import com.transitshield.backend.dto.RewardTransactionDto;
import com.transitshield.backend.dto.PassengerBalanceDto;
import com.transitshield.backend.dto.TransferRequest;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/me/public-id")
    public ResponseEntity<Map<String, String>> getMyPublicId(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("publicUserId", rewardService.getUserPublicId(user.getId())));
    }

    @GetMapping("/me/balance")
    public ResponseEntity<Map<String, Double>> getMyBalance(@AuthenticationPrincipal User user) {
        PassengerBalanceDto snapshot = rewardService.getBalanceSnapshot(user.getId());
        return ResponseEntity.ok(Map.of(
                "points", snapshot.getTotalPoints() != null ? snapshot.getTotalPoints() : 0.0,
                "totalPoints", snapshot.getTotalPoints() != null ? snapshot.getTotalPoints() : 0.0,
                "walletBalance", snapshot.getWalletBalance() != null ? snapshot.getWalletBalance() : 0.0
        ));
    }

    @GetMapping("/me/summary")
    public ResponseEntity<PassengerBalanceDto> getMyBalanceSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rewardService.getBalanceSnapshot(user.getId()));
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<RewardTransactionDto>> getMyHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rewardService.getHistory(user.getId()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<RewardTransactionDto> transferPoints(@AuthenticationPrincipal User user, @RequestBody TransferRequest request) {
        return ResponseEntity.ok(rewardService.transferPoints(user.getId(), request));
    }
}
