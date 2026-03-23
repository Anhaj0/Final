package com.transitshield.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
@Getter
@Setter
@NoArgsConstructor
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_profile_id", nullable = false)
    private PassengerProfile passengerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @Column(nullable = false)
    private Double points;

    @Column(nullable = false, length = 60)
    private String taskType;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
