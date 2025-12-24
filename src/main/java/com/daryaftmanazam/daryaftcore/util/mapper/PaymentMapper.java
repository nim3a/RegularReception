package com.daryaftmanazam.daryaftcore.util.mapper;

import com.daryaftmanazam.daryaftcore.dto.request.PaymentRequest;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentResponse;
import com.daryaftmanazam.daryaftcore.model.Payment;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

/**
 * Mapper utility for converting between Payment entity and DTOs.
 */
@UtilityClass
public class PaymentMapper {

    /**
     * Convert PaymentRequest to Payment entity (partial mapping).
     * Note: Subscription must be set separately.
     */
    public Payment toEntity(PaymentRequest request) {
        if (request == null) {
            return null;
        }

        return Payment.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .build();
    }

    /**
     * Convert Payment entity to PaymentResponse.
     */
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        String customerName = payment.getSubscription() != null && payment.getSubscription().getCustomer() != null ?
            payment.getSubscription().getCustomer().getFirstName() + " " + 
            payment.getSubscription().getCustomer().getLastName() : null;

        String planName = payment.getSubscription() != null && payment.getSubscription().getPaymentPlan() != null ?
            payment.getSubscription().getPaymentPlan().getPlanName() : null;

        BigDecimal totalAmount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
        if (payment.getLateFee() != null) {
            totalAmount = totalAmount.add(payment.getLateFee());
        }

        boolean isLate = payment.getDueDate() != null && 
            payment.getPaymentDate() != null &&
            payment.getPaymentDate().toLocalDate().isAfter(payment.getDueDate());

        Long daysLate = null;
        if (isLate && payment.getDueDate() != null && payment.getPaymentDate() != null) {
            daysLate = ChronoUnit.DAYS.between(payment.getDueDate(), payment.getPaymentDate().toLocalDate());
        }

        PaymentResponse response = PaymentResponse.builder()
                .id(payment.getId())
                .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                .customerName(customerName)
                .planName(planName)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .dueDate(payment.getDueDate())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .lateFee(payment.getLateFee())
                .totalAmount(totalAmount)
                .notes(payment.getNotes())
                .isLate(isLate)
                .daysLate(daysLate)
                .build();

        // Set formatted dates using the getter methods
        response.setFormattedPaymentDate(response.getFormattedPaymentDate());
        response.setFormattedDueDate(response.getFormattedDueDate());

        return response;
    }
}
