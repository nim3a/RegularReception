package com.daryaftmanazam.daryaftcore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Refresh token request DTO
 */
@Data
public class RefreshTokenRequest {
    
    @NotBlank(message = "توکن تازه‌سازی الزامی است")
    private String refreshToken;
}
