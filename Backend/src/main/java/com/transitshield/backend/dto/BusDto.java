package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.BusStatus;
import lombok.Data;

@Data
public class BusDto {
    private Long id;
    private String busCode;
    private String registrationNumber;
    private String busDisplayName;
    private Integer capacity;
    private String operatorName;
    private BusStatus status;
}
