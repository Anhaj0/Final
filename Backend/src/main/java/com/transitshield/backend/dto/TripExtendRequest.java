package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class TripExtendRequest {
    private Long tripId;
    private Long newDestinationStopId;
}
