package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.ExtensionStatus;
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
public class TripExtension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_trip_id", nullable = false)
    private PassengerTrip passengerTrip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_destination_stop_id")
    private Stop previousDestinationStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_destination_stop_id")
    private Stop newDestinationStop;

    private Double additionalFareLkr = 0.0;

    @Enumerated(EnumType.STRING)
    private ExtensionStatus extensionStatus = ExtensionStatus.REQUESTED;

    private LocalDateTime createdAt = LocalDateTime.now();
}
