package com.daryaftmanazam.daryaftcore.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Subscription ID is required")
    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    @JsonProperty("payment_method")
    private String paymentMethod;
    
    @JsonProperty("transaction_id")
    private String transactionId;
    
    @JsonProperty("notes")
    private String notes;
}
