package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class AlertDto {
    private String id;
    private String type; // COMPLAINT, LOST_ITEM, SYSTEM
    private String title;
    private String message;
    private String timestamp;
}
