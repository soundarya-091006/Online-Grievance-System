package com.safereport.service.impl;

import com.safereport.dto.request.ChangePasswordRequest;
import com.safereport.dto.request.UpdateProfileRequest;
import com.safereport.entity.User;
import com.safereport.enums.Role;
import com.safereport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getProfile(User user) {
        return user;
    }

    public User updateProfile(UpdateProfileRequest req, User user) {
        if (req.getFullName() != null && !req.getFullName().isBlank())
            user.setFullName(req.getFullName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        return userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequest req, User user) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // ─── Admin operations ─────────────────────────────────────────────────────

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    public List<User> getAuthorities() {
        return userRepository.findByRole(Role.AUTHORITY);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User toggleUserStatus(Long userId) {
        User user = getUserById(userId);
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public User createAuthority(String fullName, String email, String password, String phone, String address) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("A user with this email already exists");
        }
        User authority = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phone(phone)
                .address(address)
                .role(Role.AUTHORITY)
                .active(true)
                .emailVerified(true)
                .build();
        return userRepository.save(authority);
    }
}
