package com.uep.pillar.dto;

import com.uep.pillar.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses containing JWT token and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private User user;
    private Long expiresIn;
    private String tokenType;
}
