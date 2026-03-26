package com.transitshield.backend.entity;

import com.transitshield.backend.entity.enums.ServiceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    private String variantCode;

    private String originName;

    private String destinationName;

    private String directionLabel;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private String notes;

    private Boolean isActive = true;
}
