package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_profile_id", nullable = false)
    private DriverProfile driverProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_variant_id", nullable = false)
    private RouteVariant routeVariant;

    @OneToMany(mappedBy = "busAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PassengerTrip> passengerTrips = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AssignmentStatus assignmentStatus = AssignmentStatus.ACTIVE;

    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime endedAt;
}
