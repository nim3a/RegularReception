package com.daryaftmanazam.daryaftcore.util.mapper;

import com.daryaftmanazam.daryaftcore.dto.request.PaymentPlanRequest;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentPlanResponse;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mapper utility for converting between PaymentPlan entity and DTOs.
 */
@UtilityClass
public class PaymentPlanMapper {

    /**
     * Convert PaymentPlanRequest to PaymentPlan entity.
     */
    public PaymentPlan toEntity(PaymentPlanRequest request) {
        if (request == null) {
            return null;
        }

        return PaymentPlan.builder()
                .planName(request.getPlanName())
                .periodType(request.getPeriodType())
                .periodCount(request.getPeriodCount())
                .baseAmount(request.getBaseAmount())
                .discountPercentage(request.getDiscountPercentage())
                .lateFeePerDay(request.getLateFeePerDay())
                .gracePeriodDays(request.getGracePeriodDays())
                .isActive(true)
                .build();
    }

    /**
     * Update PaymentPlan entity from PaymentPlanRequest.
     */
    public void updateEntity(PaymentPlanRequest request, PaymentPlan plan) {
        if (request == null || plan == null) {
            return;
        }

        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        if (request.getPeriodType() != null) {
            plan.setPeriodType(request.getPeriodType());
        }
        if (request.getPeriodCount() != null) {
            plan.setPeriodCount(request.getPeriodCount());
        }
        if (request.getBaseAmount() != null) {
            plan.setBaseAmount(request.getBaseAmount());
        }
        if (request.getDiscountPercentage() != null) {
            plan.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getLateFeePerDay() != null) {
            plan.setLateFeePerDay(request.getLateFeePerDay());
        }
        if (request.getGracePeriodDays() != null) {
            plan.setGracePeriodDays(request.getGracePeriodDays());
        }
    }

    /**
     * Convert PaymentPlan entity to PaymentPlanResponse.
     */
    public PaymentPlanResponse toResponse(PaymentPlan plan) {
        if (plan == null) {
            return null;
        }

        BigDecimal finalAmount = calculateFinalAmount(plan.getBaseAmount(), plan.getDiscountPercentage());

        PaymentPlanResponse response = PaymentPlanResponse.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .periodType(plan.getPeriodType())
                .periodCount(plan.getPeriodCount())
                .baseAmount(plan.getBaseAmount())
                .discountPercentage(plan.getDiscountPercentage())
                .lateFeePerDay(plan.getLateFeePerDay())
                .gracePeriodDays(plan.getGracePeriodDays())
                .isActive(plan.getIsActive())
                .businessId(plan.getBusiness() != null ? plan.getBusiness().getId() : null)
                .finalAmount(finalAmount)
                .build();

        // Set formatted amounts using the getter methods
        response.setFormattedAmount(response.getFormattedAmount());
        response.setFormattedFinalAmount(response.getFormattedFinalAmount());

        return response;
    }

    /**
     * Calculate final amount after discount.
     */
    private BigDecimal calculateFinalAmount(BigDecimal baseAmount, BigDecimal discountPercentage) {
        if (baseAmount == null) {
            return BigDecimal.ZERO;
        }
        if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return baseAmount;
        }

        BigDecimal discountAmount = baseAmount
                .multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        return baseAmount.subtract(discountAmount);
    }
}
