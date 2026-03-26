package com.transitshield.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DriverScheduleDto {
    private Long assignmentId;

    private Long driverProfileId;
    private Long driverUserId;
    private String driverName;
    private String driverCode;
    private String depot;

    private Long busId;
    private String busCode;
    private String busDisplayName;
    private String registrationNumber;
    private Integer capacity;
    private String operatorName;
    private String busStatus;

    private Long routeVariantId;
    private String routeVariantCode;
    private String routeNumber;
    private String routeName;
    private String originName;
    private String destinationName;
    private String directionLabel;

    private String assignmentStatus;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private Boolean hasActiveQr;
    private Long activeQrId;
    private String activeQrLabel;
    private String activeQrToken;
}
