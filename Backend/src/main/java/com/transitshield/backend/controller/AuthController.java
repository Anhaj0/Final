package com.transitshield.backend.controller;

import com.transitshield.backend.dto.auth.AuthResponse;
import com.transitshield.backend.dto.auth.LoginRequest;
import com.transitshield.backend.dto.auth.RegisterRequest;
import com.transitshield.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/register/passenger")
    public ResponseEntity<AuthResponse> registerPassenger(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerPassenger(request));
    }

    // Driver registration removed from public endpoint.
    // Drivers are created by admin only via /api/admin/drivers

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
