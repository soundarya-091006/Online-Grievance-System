package com.safereport.controller;

import com.safereport.dto.response.ApiResponse;
import com.safereport.entity.AuditLog;
import com.safereport.entity.User;
import com.safereport.enums.Role;
import com.safereport.repository.AuditLogRepository;
import com.safereport.service.impl.AnalyticsService;
import com.safereport.service.impl.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AnalyticsService analyticsService;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardStats()));
    }

    // ─── User Management ──────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(name = "role", required = false) Role role,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (role != null) {
            return ResponseEntity.ok(ApiResponse.success(userService.getUsersByRole(role, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    @GetMapping("/authorities")
    public ResponseEntity<ApiResponse<List<User>>> getAuthorities() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAuthorities()));
    }

    @PatchMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<User>> toggleStatus(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("User status updated", userService.toggleUserStatus(userId)));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // ─── Authority Management ──────────────────────────────────────────────────

    @PostMapping("/authorities")
    public ResponseEntity<ApiResponse<User>> createAuthority(@RequestBody Map<String, String> body) {
        String fullName = body.get("fullName");
        String email    = body.get("email");
        String password = body.get("password");
        String phone    = body.get("phone");
        String address  = body.get("address");

        if (fullName == null || email == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Full name, email, and password are required"));
        }

        User authority = userService.createAuthority(fullName, email, password, phone, address);
        return ResponseEntity.ok(ApiResponse.success("Authority created successfully", authority));
    }

    // ─── Audit Logs ───────────────────────────────────────────────────────────

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = userId != null
                ? auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
