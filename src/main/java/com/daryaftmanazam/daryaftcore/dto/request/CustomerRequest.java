package com.daryaftmanazam.daryaftcore.dto.request;

import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a customer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^09\\d{9}$", message = "Phone number must be in Iranian format: 09xxxxxxxxx")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @JsonProperty("email")
    private String email;

    @NotNull(message = "Customer type is required")
    @JsonProperty("customer_type")
    private CustomerType customerType;
    
    @JsonProperty("join_date")
    private java.time.LocalDate joinDate;
}
