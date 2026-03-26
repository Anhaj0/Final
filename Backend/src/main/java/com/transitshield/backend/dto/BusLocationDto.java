package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.OccupancyStatus;
import com.transitshield.backend.entity.enums.SourceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BusLocationDto {
    private Long id;
    private Long busId;
    private Long routeVariantId;
    private Long driverProfileId;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double heading;
    private OccupancyStatus occupancyStatus;
    private LocalDateTime recordedAt;
    private SourceType sourceType;
}
