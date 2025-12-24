package com.daryaftmanazam.daryaftcore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for all API responses.
 *
 * @param <T> the type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation completed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a successful response with data and custom message.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a successful response without data.
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with data.
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
