package com.safereport.service.impl;

import com.safereport.dto.request.LoginRequest;
import com.safereport.dto.request.RegisterRequest;
import com.safereport.dto.response.AuthResponse;
import com.safereport.entity.User;
import com.safereport.enums.Role;
import com.safereport.repository.UserRepository;
import com.safereport.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final String AUTHORITY_VERIFICATION_CODE = "AUTH-CODE-001";

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Block ADMIN self-registration
        if (req.getRole() == Role.ADMIN) {
            throw new RuntimeException("Admin accounts cannot be self-registered");
        }

        // AUTHORITY requires verification code
        if (req.getRole() == Role.AUTHORITY) {
            if (req.getVerificationCode() == null ||
                    !AUTHORITY_VERIFICATION_CODE.equals(req.getVerificationCode())) {
                throw new RuntimeException("Invalid authority verification code");
            }
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .role(req.getRole() != null ? req.getRole() : Role.USER)
                .active(true)
                .emailVerified(false)
                .emailVerificationCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .build();

        userRepository.save(user);
        log.info("New user registered: {} [{}]", user.getEmail(), user.getRole());

        // Send welcome email
        emailService.sendWelcomeEmail(user);

        String token = jwtUtil.generateToken(user);
        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);
        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, token);
    }

    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!code.equals(user.getEmailVerificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }
        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        userRepository.save(user);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email"));
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user, resetToken);
        log.info("Password reset email sent to: {}", email);
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        if (user.getPasswordResetExpiry() == null ||
                user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
        log.info("Password reset successfully for: {}", user.getEmail());
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }
}