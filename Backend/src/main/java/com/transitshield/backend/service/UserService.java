package com.transitshield.backend.service;

import com.transitshield.backend.dto.UserDto;
import com.transitshield.backend.entity.PassengerProfile;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.UserRole;
import com.transitshield.backend.repository.PassengerProfileRepository;
import com.transitshield.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<UserDto> findByRole(UserRole role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Long id) {
        return userRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    public UserDto create(UserDto dto) {
        User user = mapToEntity(dto);
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user = userRepository.save(user);
        return mapToDto(user);
    }

    public UserDto update(Long id, UserDto dto) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(dto.getFullName());
            user.setAge(dto.getAge());
            user.setEmail(dto.getEmail());
            user.setPhoneNumber(dto.getPhoneNumber());
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            user.setRole(dto.getRole());
            if (dto.getIsActive() != null) user.setIsActive(dto.getIsActive());
            return mapToDto(userRepository.save(user));
        }).orElse(null);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setAge(user.getAge());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        // Do NOT return password hash
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        if (user.getRole() == UserRole.PASSENGER) {
            PassengerProfile profile = passengerProfileRepository.findByUserId(user.getId()).orElse(null);
            dto.setWalletBalance(profile != null ? profile.getWalletBalance() : 0.0);
        } else {
            dto.setWalletBalance(0.0);
        }
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private User mapToEntity(UserDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setFullName(dto.getFullName());
        user.setAge(dto.getAge());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return user;
    }
}
