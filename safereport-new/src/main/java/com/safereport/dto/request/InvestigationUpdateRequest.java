package com.safereport.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InvestigationUpdateRequest {

    @NotBlank(message = "Description is required")
    private String description;

    private String updateType = "INVESTIGATION_NOTE";
    private boolean notifyComplainant = false;
}
