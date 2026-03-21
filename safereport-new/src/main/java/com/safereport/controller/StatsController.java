package com.safereport.controller;

import com.safereport.dto.response.ApiResponse;
import com.safereport.enums.ComplaintStatus;
import com.safereport.enums.Role;
import com.safereport.repository.ComplaintRepository;
import com.safereport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    /**
     * Public endpoint — no auth required.
     * Returns real platform statistics for the landing page.
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Map<String, Object>>> publicStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long total    = complaintRepository.count();
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long closed   = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long rejected = complaintRepository.countByStatus(ComplaintStatus.REJECTED);
        long users    = userRepository.countByRole(Role.USER);

        long resolvedTotal = resolved + closed;
        long resolutionRate = total > 0
                ? Math.round((resolvedTotal * 100.0) / total)
                : 0;

        stats.put("totalComplaints",  total);
        stats.put("resolvedComplaints", resolvedTotal);
        stats.put("resolutionRate",   resolutionRate);
        stats.put("totalUsers",       users);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}