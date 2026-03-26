package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class BusQrCodeDto {
    private Long id;
    private Long busId;
    private String qrToken;
    private String qrLabel;
    private Boolean isActive;
}
