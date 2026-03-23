package com.transitshield.backend.controller;

import com.transitshield.backend.dto.UserDto;
import com.transitshield.backend.entity.enums.UserRole;
import com.transitshield.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.transitshield.backend.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll(@RequestParam(value = "role", required = false) String role) {
        if (role != null) {
            try {
                UserRole userRole = UserRole.valueOf(role.toUpperCase());
                return userService.findByRole(userRole);
            } catch (IllegalArgumentException ignored) {
                // invalid role string, return all
            }
        }
        return userService.findAll();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal User user) {
        UserDto dto = userService.findById(user.getId());
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@AuthenticationPrincipal User user, @RequestBody UserDto dto) {
        // Prevent changing core identities or roles
        UserDto safeDto = userService.findById(user.getId());
        if (safeDto == null) return ResponseEntity.notFound().build();
        safeDto.setFullName(dto.getFullName());
        safeDto.setAge(dto.getAge());
        safeDto.setPhoneNumber(dto.getPhoneNumber());
        // Email intentionally not updatable via this basic endpoint to prevent auth issues, or allow it safely:
        safeDto.setEmail(dto.getEmail()); 
        
        UserDto updated = userService.update(user.getId(), safeDto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable("id") Long id) {
        UserDto dto = userService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable("id") Long id, @RequestBody UserDto dto) {
        UserDto updated = userService.update(id, dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
