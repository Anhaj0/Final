package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.BoardingDetectMethod;
import com.transitshield.backend.entity.enums.PaymentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PassengerTripDto {
    private Long id;
    private String tripRef;
    private Long passengerProfileId;
    private Long busAssignmentId;
    private Long busId;
    private String busDisplayName;
    private Long routeVariantId;
    private String routeName;
    private String driverName;
    private String qrTokenUsed;
    private Long boardingStopId;
    private String boardingStopName;
    private BoardingDetectMethod boardingDetectMethod;
    private Long selectedDestinationStopId;
    private String selectedDestinationStopName;
    private Long actualExitStopId;
    private Double baseFareLkr;
    private Double extraFareLkr;
    private Double totalFareLkr;
    private Double walletBalanceAfterPayment;
    private PaymentStatus paymentStatus;
    private TripStatus tripStatus;
    private Boolean ticketVerified;
    private LocalDateTime ticketVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
