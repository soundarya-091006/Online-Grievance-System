package com.safereport.dto.request;

import com.safereport.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ComplaintRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private Long categoryId;
    private Priority priority;
    private String location;
    private LocalDate incidentDate;
    private boolean anonymous = false;
}
