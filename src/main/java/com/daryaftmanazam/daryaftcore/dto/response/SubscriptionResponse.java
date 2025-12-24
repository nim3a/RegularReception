package com.daryaftmanazam.daryaftcore.dto.response;

import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for subscription entity with customer and plan details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    @JsonProperty("id")
    private Long id;

    // Customer information
    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("customer_phone")
    private String customerPhone;

    // Payment plan details
    @JsonProperty("payment_plan_id")
    private Long paymentPlanId;

    @JsonProperty("plan_name")
    private String planName;
    
    @JsonProperty("payment_plan_name")
    private String paymentPlanName;

    @JsonProperty("period_type")
    private PeriodType periodType;

    @JsonProperty("period_count")
    private Integer periodCount;

    // Subscription details
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("status")
    private SubscriptionStatus status;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("discount_applied")
    private BigDecimal discountApplied;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("next_payment_date")
    private LocalDate nextPaymentDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("last_payment_date")
    private LocalDate lastPaymentDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // Payment status information
    @JsonProperty("total_paid")
    private BigDecimal totalPaid;

    @JsonProperty("remaining_balance")
    private BigDecimal remainingBalance;

    @JsonProperty("payment_count")
    private Integer paymentCount;

    @JsonProperty("is_overdue")
    private Boolean isOverdue;

    @JsonProperty("days_until_next_payment")
    private Long daysUntilNextPayment;
    
    @JsonProperty("days_overdue")
    private Integer daysOverdue;
}
