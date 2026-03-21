package com.safereport.controller;

import com.safereport.dto.request.AssignComplaintRequest;
import com.safereport.dto.request.InvestigationUpdateRequest;
import com.safereport.dto.request.UpdateStatusRequest;
import com.safereport.dto.response.ApiResponse;
import com.safereport.dto.response.ComplaintResponse;
import com.safereport.entity.ComplaintUpdate;
import com.safereport.dto.response.EvidenceResponse;
import com.safereport.entity.User;
import com.safereport.enums.ComplaintStatus;
import com.safereport.enums.Priority;
import com.safereport.service.impl.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    // ─── PUBLIC ───────────────────────────────────────────────────────────────

    /**
     * Public: submit a complaint (anonymous allowed)
     * Accepts multipart/form-data with JSON complaint + optional files
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ComplaintResponse>> submit(
            @RequestPart("complaint") String complaintJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User user) {
        ComplaintResponse response = complaintService.submitComplaint(complaintJson, files, user);
        return ResponseEntity.ok(ApiResponse.success("Complaint submitted successfully", response));
    }

    /**
     * Public: track a complaint by tracking ID (no auth required)
     */
    @GetMapping("/track/{trackingId}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> track(
            @PathVariable("trackingId") String trackingId) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.trackComplaint(trackingId)));
    }

    // ─── USER ─────────────────────────────────────────────────────────────────

    /**
     * Get complaints submitted by the logged-in user
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> myComplaints(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getMyComplaints(user, pageable)));
    }

    /**
     * Get a single complaint by ID (user must own it, or be AUTHORITY/ADMIN)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getComplaintById(id)));
    }

    /**
     * Get evidence list for a complaint
     */
    @GetMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<List<EvidenceResponse>>> getEvidence(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getEvidence(id)));
    }

    /**
     * Get update history for a complaint
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<ComplaintUpdate>>> getHistory(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getComplaintHistory(id)));
    }

    // ─── ADMIN ────────────────────────────────────────────────────────────────

    /**
     * Admin: get all complaints with optional filters
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> getAllComplaints(
            @RequestParam(name = "status", required = false) ComplaintStatus status,
            @RequestParam(name = "priority", required = false) Priority priority,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getAllComplaints(status, priority, categoryId, keyword, pageable)));
    }

    /**
     * Admin: assign complaint to an authority
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assign(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssignComplaintRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(
                "Complaint assigned successfully",
                complaintService.assignComplaint(id, req, admin)));
    }

    /**
     * Admin or Authority: update complaint status / priority
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUTHORITY')")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody UpdateStatusRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                "Status updated successfully",
                complaintService.updateStatus(id, req, user)));
    }

    // ─── AUTHORITY ────────────────────────────────────────────────────────────

    /**
     * Authority: get complaints assigned to me
     */
    @GetMapping("/authority/assigned")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> assignedToMe(
            @RequestParam(name = "status", required = false) ComplaintStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @AuthenticationPrincipal User authority) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                complaintService.getAuthorityComplaints(authority, status, pageable)));
    }

    /**
     * Authority: add investigation note / update
     */
    @PostMapping("/{id}/investigation-update")
    @PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> addInvestigationUpdate(
            @PathVariable("id") Long id,
            @Valid @RequestBody InvestigationUpdateRequest req,
            @AuthenticationPrincipal User authority) {
        complaintService.addInvestigationUpdate(id, req, authority);
        return ResponseEntity.ok(ApiResponse.success("Update added successfully", null));
    }
}