package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class FarePreviewRequest {
    private Long routeVariantId;
    private Long boardingStopId;
    private Long destinationStopId;
}
