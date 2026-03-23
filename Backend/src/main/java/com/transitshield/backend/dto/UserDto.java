package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String fullName;
    private Integer age;
    private String email;
    private String phoneNumber;
    private String password;
    private UserRole role;
    private Boolean isActive;
    private Double walletBalance;
    private LocalDateTime createdAt;
}
