package com.daryaftmanazam.daryaftcore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for payment initiation containing payment URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitResponse {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("payment_url")
    private String paymentUrl;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("error_message")
    private String errorMessage;
}
