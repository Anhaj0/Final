package com.transitshield.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class QrScanResponse {
    private String message;
    private Long busId;
    private String busDisplayName;
    private Long driverProfileId;
    private Long routeVariantId;
    private Long busAssignmentId;
    private Long nearestBoardingStopId;
    private List<StopDto> orderedStops;
}
