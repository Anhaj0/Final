package com.transitshield.backend.dto;

import lombok.Data;

@Data
public class RouteDto {
    private Long id;
    private String routeNumber;
    private String displayName;
    private String routeCategory;
    private Boolean isActive;
}
