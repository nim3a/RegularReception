package com.daryaftmanazam.daryaftcore.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotNull(message = "Customer ID is required")
    @JsonProperty("customer_id")
    private Long customerId;

    @NotNull(message = "Payment plan ID is required")
    @JsonProperty("payment_plan_id")
    private Long paymentPlanId;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("start_date")
    private LocalDate startDate;
    
    @JsonProperty("number_of_periods")
    private Integer numberOfPeriods;
}
