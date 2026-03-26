package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class TransferRequest {
    private String recipientPublicId;
    private Double amount;
}
