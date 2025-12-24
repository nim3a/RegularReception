package com.daryaftmanazam.daryaftcore.dto.response;

import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Response DTO for payment entity with formatted dates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("subscription_id")
    private Long subscriptionId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("plan_name")
    private String planName;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("payment_date")
    private LocalDateTime paymentDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("due_date")
    private LocalDate dueDate;

    @JsonProperty("status")
    private PaymentStatus status;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("late_fee")
    private BigDecimal lateFee;

    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @JsonProperty("notes")
    private String notes;

    // Formatted dates
    @JsonProperty("formatted_payment_date")
    private String formattedPaymentDate;

    @JsonProperty("formatted_due_date")
    private String formattedDueDate;

    @JsonProperty("is_late")
    private Boolean isLate;

    @JsonProperty("days_late")
    private Long daysLate;

    /**
     * Get formatted payment date.
     */
    public String getFormattedPaymentDate() {
        if (paymentDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return paymentDate.format(formatter);
    }

    /**
     * Get formatted due date.
     */
    public String getFormattedDueDate() {
        if (dueDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dueDate.format(formatter);
    }
}
