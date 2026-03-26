package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class TripStartRequest {
    private Long passengerProfileId;
    private Long busAssignmentId;
    private String qrTokenUsed;
    private Long boardingStopId;
    private Long selectedDestinationStopId;
}
