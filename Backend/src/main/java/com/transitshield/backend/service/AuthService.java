package com.transitshield.backend.service;

import com.transitshield.backend.dto.auth.AuthResponse;
import com.transitshield.backend.dto.auth.LoginRequest;
import com.transitshield.backend.dto.auth.RegisterRequest;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.PassengerProfile;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.UserRole;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.DuplicateResourceException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.PassengerProfileRepository;
import com.transitshield.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse registerPassenger(RegisterRequest request) {
        validateNewUser(request);

        User user = createUser(request, UserRole.PASSENGER);
        
        PassengerProfile profile = new PassengerProfile();
        profile.setUser(user);
        profile.setPublicUserId("TSP" + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        profile.setWalletBalance(0.0);
        profile.setTotalPoints(0.0);
        PassengerProfile savedProfile = passengerProfileRepository.save(profile);
        if (savedProfile.getId() == null) {
            throw new BadRequestException("Failed to create passenger profile");
        }

        return new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getToken(), "Passenger registered successfully");
    }

    public AuthResponse registerDriver(RegisterRequest request) {
        validateNewUser(request);

        User user = createUser(request, UserRole.DRIVER);

        DriverProfile profile = new DriverProfile();
        profile.setUser(user);
        profile.setDriverCode("DRV-" + user.getId()); // placeholder auto generation
        profile.setDemeritPoints(0);
        driverProfileRepository.save(profile);

        return new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getToken(), "Driver registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is disabled");
        }

        // Generate token on login if not exists or rotate it
        user.setToken(java.util.UUID.randomUUID().toString());
        userRepository.save(user);

        return new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getToken(), "Login successful");
    }

    private void validateNewUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }
        if (request.getPhoneNumber() != null && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already in use");
        }
    }

    private User createUser(RegisterRequest request, UserRole role) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setToken(java.util.UUID.randomUUID().toString());
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
