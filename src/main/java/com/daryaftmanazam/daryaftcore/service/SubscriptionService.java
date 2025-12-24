package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.dto.request.SubscriptionRequest;
import com.daryaftmanazam.daryaftcore.dto.response.SubscriptionResponse;
import com.daryaftmanazam.daryaftcore.exception.CustomerNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.InvalidSubscriptionException;
import com.daryaftmanazam.daryaftcore.exception.PaymentPlanNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.SubscriptionNotFoundException;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.PeriodType;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import com.daryaftmanazam.daryaftcore.repository.PaymentPlanRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing subscription operations with complex business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PaymentPlanRepository paymentPlanRepository;
    private final PaymentPlanService paymentPlanService;
    
    private static final int MAX_OVERDUE_DAYS_BEFORE_DEACTIVATION = 30;
    
    /**
     * Create a new subscription.
     */
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        log.info("Creating subscription for customer: {}", request.getCustomerId());
        
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));
        
        PaymentPlan paymentPlan = paymentPlanRepository.findById(request.getPaymentPlanId())
                .orElseThrow(() -> new PaymentPlanNotFoundException(request.getPaymentPlanId()));
        
        if (!paymentPlan.getIsActive()) {
            throw new InvalidSubscriptionException("Payment plan is not active");
        }
        
        // Calculate subscription details
        LocalDate startDate = request.getStartDate();
        int periods = request.getNumberOfPeriods() != null ? request.getNumberOfPeriods() : 1;
        
        // Calculate end date based on period type and count
        LocalDate endDate = calculateEndDate(startDate, paymentPlan.getPeriodType(), 
                paymentPlan.getPeriodCount() * periods);
        
        // Calculate total amount with discount if applicable
        BigDecimal totalAmount = paymentPlanService.calculateTotalAmount(paymentPlan, periods);
        
        // Calculate discount applied
        BigDecimal baseTotal = paymentPlan.getBaseAmount().multiply(BigDecimal.valueOf(periods));
        BigDecimal discountApplied = baseTotal.subtract(totalAmount);
        
        // Create subscription
        Subscription subscription = Subscription.builder()
                .customer(customer)
                .paymentPlan(paymentPlan)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(totalAmount)
                .discountApplied(discountApplied)
                .nextPaymentDate(startDate)
                .build();
        
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully with id: {}", savedSubscription.getId());
        
        return mapToResponse(savedSubscription);
    }
    
    /**
     * Renew an existing subscription.
     */
    @Transactional
    public SubscriptionResponse renewSubscription(Long subscriptionId) {
        log.info("Renewing subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new InvalidSubscriptionException("Cannot renew a cancelled subscription");
        }
        
        PaymentPlan paymentPlan = subscription.getPaymentPlan();
        
        // Calculate new dates
        LocalDate newStartDate = subscription.getEndDate().plusDays(1);
        LocalDate newEndDate = calculateEndDate(newStartDate, paymentPlan.getPeriodType(), 
                paymentPlan.getPeriodCount());
        
        subscription.setStartDate(newStartDate);
        subscription.setEndDate(newEndDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setNextPaymentDate(newStartDate);
        
        // Recalculate amount (no discount on renewal, single period)
        BigDecimal newAmount = paymentPlan.getBaseAmount();
        subscription.setTotalAmount(newAmount);
        subscription.setDiscountApplied(BigDecimal.ZERO);
        
        Subscription renewedSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription renewed successfully: {}", renewedSubscription.getId());
        
        return mapToResponse(renewedSubscription);
    }
    
    /**
     * Cancel a subscription.
     */
    @Transactional
    public SubscriptionResponse cancelSubscription(Long subscriptionId) {
        log.info("Cancelling subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        
        Subscription cancelledSubscription = subscriptionRepository.save(subscription);
        log.info("Subscription cancelled successfully: {}", cancelledSubscription.getId());
        
        return mapToResponse(cancelledSubscription);
    }
    
    /**
     * Get subscription by ID.
     */
    public SubscriptionResponse getSubscriptionById(Long id) {
        log.debug("Fetching subscription with id: {}", id);
        
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
        
        return mapToResponse(subscription);
    }
    
    /**
     * Get active subscriptions for a customer.
     */
    public List<SubscriptionResponse> getActiveSubscriptions(Long customerId) {
        log.debug("Fetching active subscriptions for customer: {}", customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        
        List<Subscription> subscriptions = subscriptionRepository
                .findByCustomerIdAndStatus(customerId, SubscriptionStatus.ACTIVE);
        
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get subscriptions by status.
     */
    public List<SubscriptionResponse> getSubscriptionsByStatus(SubscriptionStatus status) {
        log.debug("Fetching subscriptions with status: {}", status);
        
        List<Subscription> subscriptions = subscriptionRepository.findByStatus(status);
        
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Check and update overdue subscriptions.
     * This method should be called by a scheduled job.
     */
    @Transactional
    public void checkAndUpdateOverdueSubscriptions() {
        log.info("Checking for overdue subscriptions");
        
        LocalDate today = LocalDate.now();
        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByStatus(SubscriptionStatus.ACTIVE);
        
        int updatedCount = 0;
        int deactivatedCount = 0;
        
        for (Subscription subscription : activeSubscriptions) {
            if (subscription.getNextPaymentDate() != null && 
                subscription.getNextPaymentDate().isBefore(today)) {
                
                long daysOverdue = ChronoUnit.DAYS.between(subscription.getNextPaymentDate(), today);
                
                // Auto-deactivate after 30 days overdue
                if (daysOverdue > MAX_OVERDUE_DAYS_BEFORE_DEACTIVATION) {
                    subscription.setStatus(SubscriptionStatus.EXPIRED);
                    deactivatedCount++;
                    log.warn("Subscription {} expired after {} days overdue", 
                            subscription.getId(), daysOverdue);
                } else if (subscription.getStatus() != SubscriptionStatus.OVERDUE) {
                    subscription.setStatus(SubscriptionStatus.OVERDUE);
                    updatedCount++;
                    log.warn("Subscription {} marked as overdue ({} days)", 
                            subscription.getId(), daysOverdue);
                }
            }
        }
        
        log.info("Overdue check completed. Updated: {}, Deactivated: {}", 
                updatedCount, deactivatedCount);
    }
    
    /**
     * Calculate next payment date based on period type.
     */
    public LocalDate calculateNextPaymentDate(Subscription subscription) {
        log.debug("Calculating next payment date for subscription: {}", subscription.getId());
        
        if (subscription.getLastPaymentDate() == null) {
            return subscription.getStartDate();
        }
        
        PaymentPlan plan = subscription.getPaymentPlan();
        return calculateEndDate(subscription.getLastPaymentDate(), 
                plan.getPeriodType(), plan.getPeriodCount());
    }
    
    /**
     * Apply late fee to a subscription based on days overdue.
     */
    @Transactional
    public BigDecimal applyLateFee(Long subscriptionId, int daysLate) {
        log.info("Applying late fee to subscription {} for {} days", subscriptionId, daysLate);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        
        PaymentPlan plan = subscription.getPaymentPlan();
        
        // Check if within grace period
        if (daysLate <= plan.getGracePeriodDays()) {
            log.debug("Within grace period, no late fee applied");
            return BigDecimal.ZERO;
        }
        
        // Calculate late fee: (daysLate - gracePeriod) * lateFeePerDay
        int chargeableDays = daysLate - plan.getGracePeriodDays();
        BigDecimal lateFee = plan.getLateFeePerDay()
                .multiply(BigDecimal.valueOf(chargeableDays))
                .setScale(2, RoundingMode.HALF_UP);
        
        log.info("Late fee calculated: {} for {} chargeable days", lateFee, chargeableDays);
        return lateFee;
    }
    
    /**
     * Apply discount to subscription based on payment plan.
     */
    public BigDecimal applyDiscount(Subscription subscription, PaymentPlan paymentPlan) {
        log.debug("Applying discount to subscription: {}", subscription.getId());
        
        if (paymentPlan.getDiscountPercentage() == null || 
            paymentPlan.getDiscountPercentage().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountAmount = subscription.getTotalAmount()
                .multiply(paymentPlan.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        log.debug("Discount applied: {}", discountAmount);
        return discountAmount;
    }
    
    /**
     * Calculate end date based on start date, period type, and count.
     */
    private LocalDate calculateEndDate(LocalDate startDate, PeriodType periodType, int periodCount) {
        return switch (periodType) {
            case DAILY -> startDate.plusDays(periodCount);
            case WEEKLY -> startDate.plusWeeks(periodCount);
            case MONTHLY -> startDate.plusMonths(periodCount);
            case QUARTERLY -> startDate.plusMonths(3L * periodCount);
            case SEMI_ANNUAL -> startDate.plusMonths(6L * periodCount);
            case YEARLY -> startDate.plusYears(periodCount);
        };
    }
    
    /**
     * Map Subscription entity to SubscriptionResponse DTO.
     */
    private SubscriptionResponse mapToResponse(Subscription subscription) {
        Integer daysOverdue = null;
        if (subscription.getStatus() == SubscriptionStatus.OVERDUE && 
            subscription.getNextPaymentDate() != null) {
            daysOverdue = (int) ChronoUnit.DAYS.between(
                    subscription.getNextPaymentDate(), LocalDate.now());
        }
        
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .customerId(subscription.getCustomer().getId())
                .customerName(subscription.getCustomer().getFirstName() + " " + 
                            subscription.getCustomer().getLastName())
                .paymentPlanId(subscription.getPaymentPlan().getId())
                .paymentPlanName(subscription.getPaymentPlan().getPlanName())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .totalAmount(subscription.getTotalAmount())
                .discountApplied(subscription.getDiscountApplied())
                .nextPaymentDate(subscription.getNextPaymentDate())
                .lastPaymentDate(subscription.getLastPaymentDate())
                .createdAt(subscription.getCreatedAt())
                .daysOverdue(daysOverdue)
                .build();
    }
}
