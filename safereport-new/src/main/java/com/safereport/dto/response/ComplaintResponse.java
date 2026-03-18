package com.safereport.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.safereport.enums.ComplaintStatus;
import com.safereport.enums.Priority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplaintResponse {
    private Long id;
    private String trackingId;
    private String title;
    private String description;
    private String categoryName;
    private Long categoryId;
    private ComplaintStatus status;
    private Priority priority;
    private boolean anonymous;
    private String location;
    private LocalDate incidentDate;
    private String complainantName;
    private Long complainantId;
    private String complainantEmail;
    private String complainantPhone;
    private String assignedAuthorityName;
    private Long assignedAuthorityId;
    private String adminNotes;
    private String resolutionNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private int evidenceCount;
    private int updateCount;
}