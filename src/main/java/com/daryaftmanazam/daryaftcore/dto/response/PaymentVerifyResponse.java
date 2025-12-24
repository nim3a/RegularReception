package com.daryaftmanazam.daryaftcore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for payment verification result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyResponse {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;
}
