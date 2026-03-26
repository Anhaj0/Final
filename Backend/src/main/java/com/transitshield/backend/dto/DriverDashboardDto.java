package com.transitshield.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DriverDashboardDto {
    private String name;
    private String profileInitial;
    private Long id;
    private Long driverProfileId;
    private Long activeAssignmentId;
    private Long activeBusId;
    private String depot;
    private Boolean isOnline;
    
    // stats
    private Integer demerits;
    private Integer maxDemerits;
    private String currentRoute;
    private Integer tripsToday;
    private Integer onTimePercentage;
    private Integer complaintsToday;
    
    private List<DriverAlertDto> alerts;
    private List<LostItemDto> lostItems;

    @Data
    public static class DriverAlertDto {
        private String type;
        private String title;
        private String message;
        private String timestamp;
    }

    @Data
    public static class LostItemDto {
        private String item;
        private String passengerName;
        private String route;
        private String time;
        private String status;
    }
}
