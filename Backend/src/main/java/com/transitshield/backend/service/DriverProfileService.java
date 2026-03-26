package com.transitshield.backend.service;

import com.transitshield.backend.dto.UserDto;
import com.transitshield.backend.entity.DriverProfile;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.DriverProfileRepository;
import com.transitshield.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DriverProfile updateById(Long id, UserDto dto) {
        DriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for ID: " + id));

        User user = profile.getUser();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }

        userRepository.save(user);
        return profile;
    }

    public void deleteById(Long id) {
        DriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver profile not found for ID: " + id));

        User user = profile.getUser();
        driverProfileRepository.delete(profile);

        if (user != null) {
            userRepository.delete(user);
        }
    }
}
