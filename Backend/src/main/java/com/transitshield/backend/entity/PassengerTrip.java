package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.BoardingDetectMethod;
import com.transitshield.backend.entity.enums.PaymentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerTrip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tripRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_profile_id", nullable = false)
    private PassengerProfile passengerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_assignment_id", nullable = false)
    private BusAssignment busAssignment;

    private String qrTokenUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boarding_stop_id", nullable = false)
    private Stop boardingStop;

    @Enumerated(EnumType.STRING)
    private BoardingDetectMethod boardingDetectMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_destination_stop_id")
    private Stop selectedDestinationStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_exit_stop_id")
    private Stop actualExitStop;

    private Double baseFareLkr = 0.0;

    private Double extraFareLkr = 0.0;

    private Double totalFareLkr = 0.0;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private TripStatus tripStatus = TripStatus.ACTIVE;

    private Boolean ticketVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime ticketVerifiedAt;

    private LocalDateTime endedAt;
}
