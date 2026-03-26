package com.transitshield.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostItemReportDto {
    private Long id;
    private String itemName;
    private String reporterName;
    private String reporterContact;
    private String routeOrBus;
    private String notes;
    private String status;
    private LocalDateTime createdAt;
}
