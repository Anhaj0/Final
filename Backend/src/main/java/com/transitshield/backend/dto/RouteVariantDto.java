package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.ServiceType;
import lombok.Data;

@Data
public class RouteVariantDto {
    private Long id;
    private Long routeId;
    private String variantCode;
    private String originName;
    private String destinationName;
    private String directionLabel;
    private ServiceType serviceType;
    private String notes;
    private Boolean isActive;
}
