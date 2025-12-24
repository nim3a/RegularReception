package com.daryaftmanazam.daryaftcore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Authentication response DTO
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String refreshToken;
    private String username;
    private String email;
    private String role;
    private Long businessId;
    private Long customerId;
}
