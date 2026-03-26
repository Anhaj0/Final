package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.OccupancyStatus;
import com.transitshield.backend.entity.enums.SourceType;
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
public class BusLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_variant_id")
    private RouteVariant routeVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_profile_id")
    private DriverProfile driverProfile;

    private Double latitude;

    private Double longitude;

    private Double speedKmh;

    private Double heading;

    @Enumerated(EnumType.STRING)
    private OccupancyStatus occupancyStatus;

    private LocalDateTime recordedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;
}
