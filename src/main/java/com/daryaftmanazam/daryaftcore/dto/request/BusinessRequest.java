package com.daryaftmanazam.daryaftcore.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a business.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 100, message = "Business name must not exceed 100 characters")
    @JsonProperty("business_name")
    private String businessName;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100, message = "Owner name must not exceed 100 characters")
    @JsonProperty("owner_name")
    private String ownerName;

    @Email(message = "Invalid email format")
    @JsonProperty("contact_email")
    private String contactEmail;

    @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be in Iranian format: 09xxxxxxxxx")
    @JsonProperty("contact_phone")
    private String contactPhone;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @JsonProperty("description")
    private String description;
}
