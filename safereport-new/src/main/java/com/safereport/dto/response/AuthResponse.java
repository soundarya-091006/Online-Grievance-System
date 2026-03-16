package com.safereport.dto.response;

import com.safereport.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private boolean emailVerified;
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
}
