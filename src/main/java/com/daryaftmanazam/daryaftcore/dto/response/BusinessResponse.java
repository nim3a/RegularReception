package com.daryaftmanazam.daryaftcore.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for business entity with additional computed fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("business_name")
    private String businessName;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("contact_phone")
    private String contactPhone;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Computed fields
    @JsonProperty("customer_count")
    private Long customerCount;

    @JsonProperty("active_subscriptions_count")
    private Long activeSubscriptionsCount;
}
