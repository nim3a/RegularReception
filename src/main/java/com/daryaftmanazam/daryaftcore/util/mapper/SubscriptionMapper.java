package com.daryaftmanazam.daryaftcore.util.mapper;

import com.daryaftmanazam.daryaftcore.dto.request.SubscriptionRequest;
import com.daryaftmanazam.daryaftcore.dto.response.SubscriptionResponse;
import com.daryaftmanazam.daryaftcore.model.Payment;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Mapper utility for converting between Subscription entity and DTOs.
 */
@UtilityClass
public class SubscriptionMapper {

    /**
     * Convert SubscriptionRequest to Subscription entity (partial mapping).
     * Note: Customer and PaymentPlan must be set separately.
     */
    public Subscription toEntity(SubscriptionRequest request) {
        if (request == null) {
            return null;
        }

        return Subscription.builder()
                .startDate(request.getStartDate())
                .build();
    }

    /**
     * Convert Subscription entity to SubscriptionResponse.
     */
    public SubscriptionResponse toResponse(Subscription subscription) {
        if (subscription == null) {
            return null;
        }

        String customerName = subscription.getCustomer() != null ?
            subscription.getCustomer().getFirstName() + " " + subscription.getCustomer().getLastName() : null;

        String customerPhone = subscription.getCustomer() != null ?
            subscription.getCustomer().getPhoneNumber() : null;

        String planName = subscription.getPaymentPlan() != null ?
            subscription.getPaymentPlan().getPlanName() : null;

        // Calculate payment statistics
        BigDecimal totalPaid = subscription.getPayments() != null ?
            subscription.getPayments().stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;

        BigDecimal remainingBalance = subscription.getTotalAmount() != null ?
            subscription.getTotalAmount().subtract(totalPaid) : BigDecimal.ZERO;

        int paymentCount = subscription.getPayments() != null ?
            subscription.getPayments().size() : 0;

        boolean isOverdue = subscription.getNextPaymentDate() != null &&
            subscription.getNextPaymentDate().isBefore(LocalDate.now());

        Long daysUntilNextPayment = subscription.getNextPaymentDate() != null ?
            ChronoUnit.DAYS.between(LocalDate.now(), subscription.getNextPaymentDate()) : null;

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .customerId(subscription.getCustomer() != null ? subscription.getCustomer().getId() : null)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .paymentPlanId(subscription.getPaymentPlan() != null ? subscription.getPaymentPlan().getId() : null)
                .planName(planName)
                .periodType(subscription.getPaymentPlan() != null ? subscription.getPaymentPlan().getPeriodType() : null)
                .periodCount(subscription.getPaymentPlan() != null ? subscription.getPaymentPlan().getPeriodCount() : null)
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .totalAmount(subscription.getTotalAmount())
                .discountApplied(subscription.getDiscountApplied())
                .nextPaymentDate(subscription.getNextPaymentDate())
                .lastPaymentDate(subscription.getLastPaymentDate())
                .createdAt(subscription.getCreatedAt())
                .totalPaid(totalPaid)
                .remainingBalance(remainingBalance)
                .paymentCount(paymentCount)
                .isOverdue(isOverdue)
                .daysUntilNextPayment(daysUntilNextPayment)
                .build();
    }
}
