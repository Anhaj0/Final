package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class FareRuleDto {
    private Long id;
    private Long routeVariantId;
    private Long boardingStopId;
    private Long destinationStopId;
    private Double fareLkr;
}
