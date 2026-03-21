package com.safereport.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.safereport.dto.request.AssignComplaintRequest;
import com.safereport.dto.request.ComplaintRequest;
import com.safereport.dto.request.InvestigationUpdateRequest;
import com.safereport.dto.request.UpdateStatusRequest;
import com.safereport.dto.response.ComplaintResponse;
import com.safereport.dto.response.EvidenceResponse;
import com.safereport.entity.*;
import com.safereport.enums.ComplaintStatus;
import com.safereport.enums.Priority;
import com.safereport.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EvidenceRepository evidenceRepository;
    private final ComplaintUpdateRepository updateRepository;
    private final NotificationService notificationService;
    private final AuditLogRepository auditLogRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ─── Submit ───────────────────────────────────────────────────────────────

    @Transactional
    public ComplaintResponse submitComplaint(String complaintJson,
                                             List<MultipartFile> files,
                                             User currentUser) {
        ComplaintRequest req = parseJson(complaintJson, ComplaintRequest.class);

        Category category = req.getCategoryId() != null
                ? categoryRepository.findById(req.getCategoryId()).orElse(null)
                : null;

        Complaint complaint = Complaint.builder()
                .trackingId(generateTrackingId())
                .title(req.getTitle())
                .description(req.getDescription())
                .category(category)
                .complainant(req.isAnonymous() ? null : currentUser)
                .status(ComplaintStatus.SUBMITTED)
                .priority(req.getPriority() != null ? req.getPriority() : Priority.MEDIUM)
                .anonymous(req.isAnonymous())
                .location(req.getLocation())
                .incidentDate(req.getIncidentDate())
                .build();

        complaint = complaintRepository.save(complaint);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    saveEvidence(file, complaint, currentUser, null);
                }
            }
        }

        if (currentUser != null && !req.isAnonymous()) {
            notificationService.createNotification(
                    currentUser,
                    "Complaint Submitted",
                    "Your complaint #" + complaint.getTrackingId() + " has been submitted.",
                    "COMPLAINT", complaint.getId());
        }

        // Audit log
        auditLogRepository.save(AuditLog.builder()
                .user(currentUser)
                .action("COMPLAINT_SUBMITTED")
                .entityType("COMPLAINT")
                .entityId(complaint.getId())
                .details("Complaint " + complaint.getTrackingId() + " submitted: " + complaint.getTitle())
                .build());

        log.info("Complaint submitted: {}", complaint.getTrackingId());
        return toResponse(complaint);
    }

    // ─── Track (public) ───────────────────────────────────────────────────────

    public ComplaintResponse trackComplaint(String trackingId) {
        Complaint c = complaintRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new RuntimeException("No complaint found with tracking ID: " + trackingId));
        return toPublicResponse(c);
    }

    // ─── My Complaints ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getMyComplaints(User user, Pageable pageable) {
        return complaintRepository.findByComplainant(user, pageable)
                .map(this::toResponse);
    }

    // ─── Single Complaint ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        return toResponse(findById(id));
    }

    // ─── Admin: All Complaints ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAllComplaints(ComplaintStatus status, Priority priority,
                                                    Long categoryId, String keyword,
                                                    Pageable pageable) {
        return complaintRepository.findWithFilters(status, priority, categoryId, keyword, pageable)
                .map(this::toResponse);
    }

    // ─── Authority: Assigned Complaints ───────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getAuthorityComplaints(User authority,
                                                          ComplaintStatus status,
                                                          Pageable pageable) {
        if (status != null) {
            return complaintRepository.findByAuthorityWithStatus(authority, status, pageable)
                    .map(this::toResponse);
        }
        return complaintRepository.findByAssignedAuthority(authority, pageable)
                .map(this::toResponse);
    }

    // ─── Admin: Assign ────────────────────────────────────────────────────────

    @Transactional
    public ComplaintResponse assignComplaint(Long complaintId, AssignComplaintRequest req,
                                             User admin) {
        Complaint complaint = findById(complaintId);
        User authority = userRepository.findById(req.getAuthorityId())
                .orElseThrow(() -> new RuntimeException("Authority not found"));

        String prevStatus = complaint.getStatus().name();
        complaint.setAssignedAuthority(authority);
        complaint.setStatus(ComplaintStatus.ASSIGNED);
        if (req.getNotes() != null) complaint.setAdminNotes(req.getNotes());
        complaintRepository.save(complaint);

        logUpdate(complaint, admin, "ASSIGNMENT", prevStatus, "ASSIGNED",
                "Assigned to " + authority.getFullName(), true);

        // Audit log
        auditLogRepository.save(AuditLog.builder()
                .user(admin)
                .action("COMPLAINT_ASSIGNED")
                .entityType("COMPLAINT")
                .entityId(complaint.getId())
                .details("Complaint " + complaint.getTrackingId() + " assigned to " + authority.getFullName()
                        + " (" + authority.getEmail() + ")")
                .build());

        notificationService.createNotification(authority,
                "New Case Assigned",
                "Complaint #" + complaint.getTrackingId() + " has been assigned to you.",
                "COMPLAINT", complaint.getId());

        if (complaint.getComplainant() != null) {
            notificationService.createNotification(complaint.getComplainant(),
                    "Complaint Assigned",
                    "Your complaint #" + complaint.getTrackingId() + " has been assigned to an authority.",
                    "COMPLAINT", complaint.getId());
        }

        return toResponse(complaint);
    }

    // ─── Update Status ────────────────────────────────────────────────────────

    @Transactional
    public ComplaintResponse updateStatus(Long complaintId, UpdateStatusRequest req,
                                          User updatedBy) {
        Complaint complaint = findById(complaintId);
        String prevStatus = complaint.getStatus().name();

        if (req.getStatus() != null) complaint.setStatus(req.getStatus());
        if (req.getPriority() != null) complaint.setPriority(req.getPriority());

        if (req.getStatus() == ComplaintStatus.RESOLVED
                || req.getStatus() == ComplaintStatus.CLOSED) {
            complaint.setResolvedAt(LocalDateTime.now());
            if (req.getNotes() != null) complaint.setResolutionNotes(req.getNotes());
        }

        complaintRepository.save(complaint);

        logUpdate(complaint, updatedBy, "STATUS_CHANGE", prevStatus,
                req.getStatus() != null ? req.getStatus().name() : prevStatus,
                req.getNotes(), req.isNotifyComplainant());

        // Audit log
        auditLogRepository.save(AuditLog.builder()
                .user(updatedBy)
                .action("STATUS_UPDATED")
                .entityType("COMPLAINT")
                .entityId(complaint.getId())
                .details("Complaint " + complaint.getTrackingId() + " status changed: "
                        + prevStatus + " → " + complaint.getStatus().name())
                .build());

        if (req.isNotifyComplainant() && complaint.getComplainant() != null) {
            notificationService.createNotification(complaint.getComplainant(),
                    "Complaint Status Updated",
                    "Your complaint #" + complaint.getTrackingId()
                            + " status changed to " + complaint.getStatus().name(),
                    "COMPLAINT", complaint.getId());
        }

        return toResponse(complaint);
    }

    // ─── Investigation Update ─────────────────────────────────────────────────

    @Transactional
    public void addInvestigationUpdate(Long complaintId, InvestigationUpdateRequest req,
                                       User authority) {
        Complaint complaint = findById(complaintId);
        logUpdate(complaint, authority, req.getUpdateType(),
                null, null, req.getDescription(), req.isNotifyComplainant());

        if (req.isNotifyComplainant() && complaint.getComplainant() != null) {
            notificationService.createNotification(complaint.getComplainant(),
                    "Investigation Update",
                    "There is a new update on your complaint #" + complaint.getTrackingId(),
                    "COMPLAINT", complaint.getId());
        }
    }

    // ─── Evidence ─────────────────────────────────────────────────────────────

    @Transactional
    public ComplaintResponse uploadEvidence(Long complaintId, MultipartFile file,
                                            String description, User user) {
        Complaint complaint = findById(complaintId);
        saveEvidence(file, complaint, user, description);
        return toResponse(complaint);
    }

    @Transactional(readOnly = true)
    public List<EvidenceResponse> getEvidence(Long complaintId) {
        return evidenceRepository.findByComplaintId(complaintId)
                .stream()
                .map(EvidenceResponse::from)
                .collect(java.util.stream.Collectors.toList());
    }

    public Evidence getEvidenceById(Long evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));
    }

    // ─── History ──────────────────────────────────────────────────────────────

    public List<ComplaintUpdate> getComplaintHistory(Long complaintId) {
        return updateRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private void saveEvidence(MultipartFile file, Complaint complaint,
                               User user, String description) {
        try {
            Path dir = Paths.get(uploadDir, String.valueOf(complaint.getId()));
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dir.resolve(filename);
            Files.write(filePath, file.getBytes());

            Evidence evidence = Evidence.builder()
                    .complaint(complaint)
                    .uploadedBy(user)
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .description(description)
                    .build();
            evidenceRepository.save(evidence);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + file.getOriginalFilename(), e);
        }
    }

    private void logUpdate(Complaint complaint, User updatedBy, String type,
                            String prevStatus, String newStatus,
                            String description, boolean notify) {
        ComplaintUpdate update = ComplaintUpdate.builder()
                .complaint(complaint)
                .updatedBy(updatedBy)
                .updateType(type)
                .previousStatus(prevStatus)
                .newStatus(newStatus)
                .description(description != null ? description : "")
                .notifyComplainant(notify)
                .build();
        updateRepository.save(update);
    }

    private Complaint findById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));
    }

    private String generateTrackingId() {
        return "SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private <T> T parseJson(String json, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON: " + e.getMessage());
        }
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    public ComplaintResponse toResponse(Complaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .trackingId(c.getTrackingId())
                .title(c.getTitle())
                .description(c.getDescription())
                .categoryId(c.getCategory() != null ? c.getCategory().getId() : null)
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : null)
                .status(c.getStatus())
                .priority(c.getPriority())
                .anonymous(c.isAnonymous())
                .location(c.getLocation())
                .incidentDate(c.getIncidentDate())
                .complainantName(c.isAnonymous() || c.getComplainant() == null
                        ? "Anonymous" : c.getComplainant().getFullName())
                .complainantId(c.isAnonymous() || c.getComplainant() == null
                        ? null : c.getComplainant().getId())
                .complainantEmail(c.isAnonymous() || c.getComplainant() == null
                        ? null : c.getComplainant().getEmail())
                .complainantPhone(c.isAnonymous() || c.getComplainant() == null
                        ? null : c.getComplainant().getPhone())
                .assignedAuthorityName(c.getAssignedAuthority() != null
                        ? c.getAssignedAuthority().getFullName() : null)
                .assignedAuthorityId(c.getAssignedAuthority() != null
                        ? c.getAssignedAuthority().getId() : null)
                .adminNotes(c.getAdminNotes())
                .resolutionNotes(c.getResolutionNotes())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .resolvedAt(c.getResolvedAt())
                .evidenceCount(c.getEvidences() != null ? c.getEvidences().size() : 0)
                .updateCount(c.getUpdates() != null ? c.getUpdates().size() : 0)
                .build();
    }

    // Minimal info for public tracking (no sensitive data)
    private ComplaintResponse toPublicResponse(Complaint c) {
        return ComplaintResponse.builder()
                .trackingId(c.getTrackingId())
                .title(c.getTitle())
                .status(c.getStatus())
                .priority(c.getPriority())
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : null)
                .location(c.getLocation())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .resolvedAt(c.getResolvedAt())
                .build();
    }
}