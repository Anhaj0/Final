package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class PassengerBalanceDto {
    private String fullName;
    private String publicUserId;
    private Double walletBalance;
    private Double totalPoints;
}
