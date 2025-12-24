package com.daryaftmanazam.daryaftcore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Error response DTO for API error responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("details")
    private List<String> details;

    @JsonProperty("field_errors")
    private Map<String, String> fieldErrors;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @JsonProperty("path")
    private String path;
    
    @JsonProperty("status")
    private Integer status;

    /**
     * Create a simple error response.
     */
    public static ErrorResponse of(String errorCode, String message) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with details.
     */
    public static ErrorResponse of(String errorCode, String message, List<String> details) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with field errors.
     */
    public static ErrorResponse of(String errorCode, String message, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with path.
     */
    public static ErrorResponse of(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
