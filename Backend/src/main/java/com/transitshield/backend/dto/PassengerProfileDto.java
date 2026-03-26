package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class PassengerProfileDto {
    private Long id;
    private Long userId;
    private Double walletBalance;
    private Integer totalPoints;
}
