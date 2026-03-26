package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class RouteVariantStopDto {
    private Long id;
    private Long routeVariantId;
    private Long stopId;
    private String stopName;
    private Double stopLatitude;
    private Double stopLongitude;
    private Integer stopOrder;
    private Double distanceFromStartKm;
    private Boolean isMajorStop;
}
