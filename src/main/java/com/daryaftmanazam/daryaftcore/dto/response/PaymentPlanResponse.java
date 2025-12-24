package com.daryaftmanazam.daryaftcore.dto.response;

import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Response DTO for payment plan entity with formatted amount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPlanResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("plan_name")
    private String planName;

    @JsonProperty("period_type")
    private PeriodType periodType;

    @JsonProperty("period_count")
    private Integer periodCount;

    @JsonProperty("base_amount")
    private BigDecimal baseAmount;

    @JsonProperty("discount_percentage")
    private BigDecimal discountPercentage;

    @JsonProperty("late_fee_per_day")
    private BigDecimal lateFeePerDay;

    @JsonProperty("grace_period_days")
    private Integer gracePeriodDays;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("business_id")
    private Long businessId;
    
    @JsonProperty("business_name")
    private String businessName;

    // Formatted fields
    @JsonProperty("formatted_amount")
    private String formattedAmount;

    @JsonProperty("final_amount")
    private BigDecimal finalAmount;

    @JsonProperty("formatted_final_amount")
    private String formattedFinalAmount;

    /**
     * Format the amount with currency.
     */
    public String getFormattedAmount() {
        if (baseAmount == null) {
            return "0";
        }
        NumberFormat formatter = NumberFormat.getInstance(Locale.of("fa", "IR"));
        return formatter.format(baseAmount) + " ریال";
    }

    /**
     * Format the final amount (after discount) with currency.
     */
    public String getFormattedFinalAmount() {
        if (finalAmount == null) {
            return getFormattedAmount();
        }
        NumberFormat formatter = NumberFormat.getInstance(Locale.of("fa", "IR"));
        return formatter.format(finalAmount) + " ریال";
    }
}
