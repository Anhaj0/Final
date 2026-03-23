package com.transitshield.backend.dto;

import com.transitshield.backend.entity.enums.LostItemStatus;
import lombok.Data;

@Data
public class LostItemReportDto {
    private Long id;
    private Long reporterUserId;
    private String reporterName;
    private String reporterPhoneNumber;
    private String reporterRole;

    private String itemTitle;
    private String description;
    private String category;
    private String routeInfo;
    private String busInfo;
    private String lostAt;
    private String contactDetails;

    private LostItemStatus status;
    private String adminNotes;
    private String resolutionNotes;

    private String createdAt;
    private String updatedAt;
}
