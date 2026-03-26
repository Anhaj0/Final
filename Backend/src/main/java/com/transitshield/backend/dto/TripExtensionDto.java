package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.ExtensionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TripExtensionDto {
    private Long id;
    private Long passengerTripId;
    private Long previousDestinationStopId;
    private Long newDestinationStopId;
    private Double additionalFareLkr;
    private ExtensionStatus extensionStatus;
    private LocalDateTime createdAt;
}
