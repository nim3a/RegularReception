package com.daryaftmanazam.daryaftcore.dto.request;

import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a payment plan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPlanRequest {

    @NotBlank(message = "Plan name is required")
    @Size(max = 100, message = "Plan name must not exceed 100 characters")
    @JsonProperty("plan_name")
    private String planName;

    @NotNull(message = "Period type is required")
    @JsonProperty("period_type")
    private PeriodType periodType;

    @NotNull(message = "Period count is required")
    @Min(value = 1, message = "Period count must be at least 1")
    @JsonProperty("period_count")
    private Integer periodCount;

    @NotNull(message = "Base amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base amount must be greater than 0")
    @JsonProperty("base_amount")
    private BigDecimal baseAmount;

    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
    @JsonProperty("discount_percentage")
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.0", message = "Late fee per day must be non-negative")
    @JsonProperty("late_fee_per_day")
    private BigDecimal lateFeePerDay;

    @Min(value = 0, message = "Grace period days must be non-negative")
    @JsonProperty("grace_period_days")
    private Integer gracePeriodDays;
}
