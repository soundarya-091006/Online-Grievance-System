package com.safereport.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignComplaintRequest {

    @NotNull(message = "Authority ID is required")
    private Long authorityId;

    private String notes;
}
