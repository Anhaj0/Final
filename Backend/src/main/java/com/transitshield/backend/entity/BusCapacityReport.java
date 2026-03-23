package com.transitshield.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bus_capacity_reports")
@Getter
@Setter
@NoArgsConstructor
public class BusCapacityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporterUser;

    @Column(nullable = false)
    private Integer passengerCount;

    @Column(nullable = false)
    private Double averageCapacity;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
