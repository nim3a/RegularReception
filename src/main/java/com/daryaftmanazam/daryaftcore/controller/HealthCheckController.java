package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.HealthCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST Controller for system health check.
 * Provides endpoints for monitoring system status.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "سلامت سیستم", description = "بررسی وضعیت و سلامت سیستم")
public class HealthCheckController {

    /**
     * Health check endpoint to verify system status.
     *
     * @return System health status
     */
    @GetMapping("/health")
    @Operation(summary = "بررسی سلامت سیستم", description = "نمایش وضعیت و سلامت سیستم دریافت منظم")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "سیستم سالم است",
                    content = @Content(schema = @Schema(implementation = HealthCheckResponse.class))
            )
    })
    public ResponseEntity<HealthCheckResponse> health() {
        HealthCheckResponse response = HealthCheckResponse.builder()
                .status("UP")
                .message("سیستم دریافت مرکزی به درستی کار می‌کند")
                .timestamp(LocalDateTime.now())
                .version("1.0.0")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
