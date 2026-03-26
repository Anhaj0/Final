package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class TripEndRequest {
    private Long tripId;
    private Long actualExitStopId;
}
