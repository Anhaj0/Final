package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class PassengerTaskCapacityRequest {
    private Long busId;
    private Integer passengerCount;
}
