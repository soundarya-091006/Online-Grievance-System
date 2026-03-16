package com.safereport.controller;

import com.safereport.dto.request.LoginRequest;
import com.safereport.dto.request.RegisterRequest;
import com.safereport.dto.response.ApiResponse;
import com.safereport.dto.response.AuthResponse;
import com.safereport.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Registration successful", authService.register(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authService.login(req)));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestBody Map<String, String> body) {
        authService.verifyEmail(body.get("email"), body.get("code"));
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("email"));
        return ResponseEntity.ok(
                ApiResponse.success("If your email exists, reset instructions have been sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody Map<String, String> body) {
        authService.resetPassword(
                body.get("token"), body.get("newPassword"), body.get("confirmPassword"));
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
