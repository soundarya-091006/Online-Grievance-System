package com.safereport.dto.request;

import com.safereport.enums.ComplaintStatus;
import com.safereport.enums.Priority;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    private ComplaintStatus status;
    private Priority priority;
    private String notes;
    private boolean notifyComplainant = true;
}
