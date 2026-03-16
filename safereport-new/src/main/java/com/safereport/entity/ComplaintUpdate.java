package com.safereport.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_updates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplaintUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;

    private String updateType;     // STATUS_CHANGE, ASSIGNMENT, INVESTIGATION_NOTE, etc.
    private String previousStatus;
    private String newStatus;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private boolean notifyComplainant = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
