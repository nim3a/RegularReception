package com.daryaftmanazam.daryaftcore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse {
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private String version;
}
