package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.OccupancyStatus;
import com.transitshield.backend.entity.enums.SourceType;
import lombok.Data;

@Data
public class LocationUpdateRequest {
    private Long busId;
    private Long driverProfileId;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double heading;
    private OccupancyStatus occupancyStatus;
    private SourceType sourceType;
}
