package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.dto.request.PaymentPlanRequest;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentPlanResponse;
import com.daryaftmanazam.daryaftcore.exception.BusinessNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.PaymentPlanNotFoundException;
import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.repository.BusinessRepository;
import com.daryaftmanazam.daryaftcore.repository.PaymentPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing payment plan operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentPlanService {
    
    private final PaymentPlanRepository paymentPlanRepository;
    private final BusinessRepository businessRepository;
    
    /**
     * Create a new payment plan for a business.
     */
    @Transactional
    public PaymentPlanResponse createPaymentPlan(Long businessId, PaymentPlanRequest request) {
        log.info("Creating payment plan for business: {}", businessId);
        
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessNotFoundException(businessId));
        
        PaymentPlan paymentPlan = PaymentPlan.builder()
                .planName(request.getPlanName())
                .periodType(request.getPeriodType())
                .periodCount(request.getPeriodCount())
                .baseAmount(request.getBaseAmount())
                .discountPercentage(request.getDiscountPercentage() != null ? 
                        request.getDiscountPercentage() : BigDecimal.ZERO)
                .lateFeePerDay(request.getLateFeePerDay() != null ? 
                        request.getLateFeePerDay() : BigDecimal.ZERO)
                .gracePeriodDays(request.getGracePeriodDays() != null ? 
                        request.getGracePeriodDays() : 0)
                .isActive(true)
                .business(business)
                .build();
        
        PaymentPlan savedPlan = paymentPlanRepository.save(paymentPlan);
        log.info("Payment plan created successfully with id: {}", savedPlan.getId());
        
        return mapToResponse(savedPlan);
    }
    
    /**
     * Get payment plan by ID.
     */
    public PaymentPlanResponse getPaymentPlanById(Long id) {
        log.debug("Fetching payment plan with id: {}", id);
        
        PaymentPlan paymentPlan = paymentPlanRepository.findById(id)
                .orElseThrow(() -> new PaymentPlanNotFoundException(id));
        
        return mapToResponse(paymentPlan);
    }
    
    /**
     * Update an existing payment plan.
     */
    @Transactional
    public PaymentPlanResponse updatePaymentPlan(Long id, PaymentPlanRequest request) {
        log.info("Updating payment plan with id: {}", id);
        
        PaymentPlan paymentPlan = paymentPlanRepository.findById(id)
                .orElseThrow(() -> new PaymentPlanNotFoundException(id));
        
        paymentPlan.setPlanName(request.getPlanName());
        paymentPlan.setPeriodType(request.getPeriodType());
        paymentPlan.setPeriodCount(request.getPeriodCount());
        paymentPlan.setBaseAmount(request.getBaseAmount());
        paymentPlan.setDiscountPercentage(request.getDiscountPercentage() != null ? 
                request.getDiscountPercentage() : BigDecimal.ZERO);
        paymentPlan.setLateFeePerDay(request.getLateFeePerDay() != null ? 
                request.getLateFeePerDay() : BigDecimal.ZERO);
        paymentPlan.setGracePeriodDays(request.getGracePeriodDays() != null ? 
                request.getGracePeriodDays() : 0);
        
        PaymentPlan updatedPlan = paymentPlanRepository.save(paymentPlan);
        log.info("Payment plan updated successfully: {}", updatedPlan.getId());
        
        return mapToResponse(updatedPlan);
    }
    
    /**
     * Delete (deactivate) a payment plan.
     */
    @Transactional
    public void deletePaymentPlan(Long id) {
        log.info("Deactivating payment plan with id: {}", id);
        
        PaymentPlan paymentPlan = paymentPlanRepository.findById(id)
                .orElseThrow(() -> new PaymentPlanNotFoundException(id));
        
        paymentPlan.setIsActive(false);
        paymentPlanRepository.save(paymentPlan);
        
        log.info("Payment plan deactivated successfully: {}", id);
    }
    
    /**
     * Get all payment plans for a business.
     */
    public List<PaymentPlanResponse> getPaymentPlansByBusiness(Long businessId) {
        log.debug("Fetching payment plans for business: {}", businessId);
        
        // Verify business exists
        if (!businessRepository.existsById(businessId)) {
            throw new BusinessNotFoundException(businessId);
        }
        
        List<PaymentPlan> plans = paymentPlanRepository.findByBusinessIdAndIsActive(businessId, true);
        return plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate total amount for a payment plan with discount applied.
     * Discount applies only for advance payment of multiple periods.
     * 
     * @param paymentPlan The payment plan
     * @param periods Number of periods paid in advance
     * @return Total amount after discount
     */
    public BigDecimal calculateTotalAmount(PaymentPlan paymentPlan, int periods) {
        log.debug("Calculating total amount for plan {} with {} periods", 
                paymentPlan.getId(), periods);
        
        if (periods <= 0) {
            throw new IllegalArgumentException("Number of periods must be positive");
        }
        
        // Base calculation: baseAmount * periods
        BigDecimal baseTotal = paymentPlan.getBaseAmount().multiply(BigDecimal.valueOf(periods));
        
        // Apply discount only if paying for multiple periods in advance
        if (periods > 1 && paymentPlan.getDiscountPercentage() != null && 
            paymentPlan.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    paymentPlan.getDiscountPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
            
            BigDecimal discountedTotal = baseTotal.multiply(discountMultiplier)
                    .setScale(2, RoundingMode.HALF_UP);
            
            log.debug("Discount applied: {}% on {} periods. Original: {}, Discounted: {}", 
                    paymentPlan.getDiscountPercentage(), periods, baseTotal, discountedTotal);
            
            return discountedTotal;
        }
        
        return baseTotal.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Map PaymentPlan entity to PaymentPlanResponse DTO.
     */
    private PaymentPlanResponse mapToResponse(PaymentPlan plan) {
        return PaymentPlanResponse.builder()
                .id(plan.getId())
                .planName(plan.getPlanName())
                .periodType(plan.getPeriodType())
                .periodCount(plan.getPeriodCount())
                .baseAmount(plan.getBaseAmount())
                .discountPercentage(plan.getDiscountPercentage())
                .lateFeePerDay(plan.getLateFeePerDay())
                .gracePeriodDays(plan.getGracePeriodDays())
                .isActive(plan.getIsActive())
                .businessId(plan.getBusiness().getId())
                .businessName(plan.getBusiness().getBusinessName())
                .build();
    }
}
