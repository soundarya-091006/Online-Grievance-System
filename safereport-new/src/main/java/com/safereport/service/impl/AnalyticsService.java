package com.safereport.service.impl;

import com.safereport.enums.ComplaintStatus;
import com.safereport.enums.Role;
import com.safereport.repository.ComplaintRepository;
import com.safereport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Complaint counts by status
        long totalComplaints = complaintRepository.count();
        long submitted    = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long underReview  = complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW);
        long assigned     = complaintRepository.countByStatus(ComplaintStatus.ASSIGNED);
        long investigating = complaintRepository.countByStatus(ComplaintStatus.INVESTIGATING);
        long resolved     = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long closed       = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long rejected     = complaintRepository.countByStatus(ComplaintStatus.REJECTED);

        stats.put("totalComplaints", totalComplaints);
        stats.put("submitted", submitted);
        stats.put("underReview", underReview);
        stats.put("assigned", assigned);
        stats.put("investigating", investigating);
        stats.put("resolved", resolved);
        stats.put("closed", closed);
        stats.put("rejected", rejected);

        // Computed aggregate fields (used by admin dashboard frontend)
        stats.put("pendingReview", submitted + underReview);
        stats.put("activeInvestigations", assigned + investigating);
        stats.put("resolvedComplaints", resolved + closed);

        // User counts
        stats.put("totalUsers", userRepository.countByRole(Role.USER));
        stats.put("totalAuthorities", userRepository.countByRole(Role.AUTHORITY));
        stats.put("activeUsers", userRepository.countByActive(true));

        // Breakdown by category
        List<Object[]> byCategory = complaintRepository.countByCategory();
        Map<String, Long> categoryBreakdown = new LinkedHashMap<>();
        for (Object[] row : byCategory) {
            categoryBreakdown.put((String) row[0], (Long) row[1]);
        }
        stats.put("byCategory", categoryBreakdown);

        return stats;
    }
}
