package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class PassengerTaskLocationRequest {
    private Long busId;
    private Double latitude;
    private Double longitude;
}
