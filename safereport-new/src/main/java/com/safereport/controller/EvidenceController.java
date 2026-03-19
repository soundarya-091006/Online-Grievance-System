package com.safereport.controller;

import com.safereport.dto.response.ApiResponse;
import com.safereport.dto.response.EvidenceResponse;
import com.safereport.entity.Evidence;
import com.safereport.entity.User;
import com.safereport.service.impl.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/evidence")
@RequiredArgsConstructor
public class EvidenceController {

    private final ComplaintService complaintService;

    /**
     * Upload evidence for a specific complaint
     */
    @PostMapping(value = "/upload/{complaintId}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Void>> upload(
            @PathVariable("complaintId") Long complaintId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "description", required = false) String description,
            @AuthenticationPrincipal User user) {
        complaintService.uploadEvidence(complaintId, file, description, user);
        return ResponseEntity.ok(ApiResponse.success("Evidence uploaded successfully", null));
    }

    /**
     * List all evidence for a complaint — accessible by ADMIN, AUTHORITY, and the complainant
     */
    @GetMapping("/complaint/{complaintId}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHORITY','USER')")
    public ResponseEntity<ApiResponse<List<Evidence>>> listEvidence(
            @PathVariable("complaintId") Long complaintId) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getEvidence(complaintId)));
    }

    /**
     * Download a specific evidence file — accessible by ADMIN and AUTHORITY
     */
    @GetMapping("/download/{evidenceId}")
    @PreAuthorize("hasAnyRole('ADMIN','AUTHORITY','USER')")
    public ResponseEntity<Resource> download(
            @PathVariable("evidenceId") Long evidenceId) {

        Evidence evidence = complaintService.getEvidenceById(evidenceId);
        File file = new File(evidence.getFilePath());

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String contentType = evidence.getFileType() != null
                ? evidence.getFileType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + evidence.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.length())
                .body(resource);
    }
}
