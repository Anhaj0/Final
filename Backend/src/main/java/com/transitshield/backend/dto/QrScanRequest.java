package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class QrScanRequest {
    private Long passengerId;
    private String qrToken;
    private Double latitude;
    private Double longitude;
}
