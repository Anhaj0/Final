package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class StopDto {
    private Long id;
    private String stopCode;
    private String stopName;
    private Double latitude;
    private Double longitude;
    private Boolean isActive;
    private Integer stopOrder;
    private Double distanceFromStartKm;
    private Boolean isMajorStop;
}
