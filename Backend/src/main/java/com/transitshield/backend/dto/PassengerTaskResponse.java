package com.transitshield.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PassengerTaskResponse {
    private String message;
    private Double awardedPoints;
    private Double totalPoints;
    private Double averageCapacity;
}
