package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class RewardTransactionDto {
    private Long id;
    private String type;
    private Double points;
    private String description;
    private String createdAt;
}
