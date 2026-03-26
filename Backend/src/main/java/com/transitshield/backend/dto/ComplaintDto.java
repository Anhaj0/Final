package com.transitshield.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintDto {
    private Long id;
    private String passengerName;
    private String passengerContact;
    private String busReference;
    private String subject;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
