package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.AssignmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BusAssignmentDto {
    private Long id;
    private Long busId;
    private Long driverProfileId;
    private Long routeVariantId;
    private AssignmentStatus assignmentStatus;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
