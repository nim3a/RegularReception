package com.daryaftmanazam.daryaftcore.service;

import com.daryaftmanazam.daryaftcore.dto.request.PaymentInitRequest;
import com.daryaftmanazam.daryaftcore.dto.request.PaymentRequest;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentDetailsDto;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentInitResponse;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentResponse;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentVerifyResponse;
import com.daryaftmanazam.daryaftcore.exception.CustomerNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.InvalidPaymentException;
import com.daryaftmanazam.daryaftcore.exception.PaymentNotFoundException;
import com.daryaftmanazam.daryaftcore.exception.SubscriptionNotFoundException;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.Payment;
import com.daryaftmanazam.daryaftcore.model.PaymentPlan;
import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import com.daryaftmanazam.daryaftcore.repository.PaymentRepository;
import com.daryaftmanazam.daryaftcore.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing payment operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionService subscriptionService;
    
    /**
     * Process a new payment.
     * Payment updates subscription status automatically.
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for subscription: {}", request.getSubscriptionId());
        
        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException(request.getSubscriptionId()));
        
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new InvalidPaymentException("Cannot process payment for cancelled subscription");
        }
        
        // Calculate late fee if payment is overdue
        BigDecimal lateFee = BigDecimal.ZERO;
        LocalDate dueDate = subscription.getNextPaymentDate();
        
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            int daysLate = (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            lateFee = subscriptionService.applyLateFee(subscription.getId(), daysLate);
        }
        
        // Generate transaction ID if not provided
        String transactionId = request.getTransactionId() != null ? 
                request.getTransactionId() : generateTransactionId();
        
        // Create payment record
        Payment payment = Payment.builder()
                .subscription(subscription)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .dueDate(dueDate)
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .lateFee(lateFee)
                .notes(request.getNotes())
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Update subscription after successful payment
        updateSubscriptionAfterPayment(subscription);
        
        log.info("Payment processed successfully with id: {}", savedPayment.getId());
        
        return mapToResponse(savedPayment);
    }
    
    /**
     * Verify a payment (mock implementation for MVP).
     */
    public PaymentResponse verifyPayment(String transactionId) {
        log.info("Verifying payment with transaction ID: {}", transactionId);
        
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Transaction ID: " + transactionId));
        
        // Mock verification - in real implementation, this would check with payment gateway
        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            
            // Update subscription after verification
            updateSubscriptionAfterPayment(payment.getSubscription());
        }
        
        log.info("Payment verified: {}", transactionId);
        return mapToResponse(payment);
    }
    
    /**
     * Get payment history for a customer.
     */
    public Page<PaymentResponse> getPaymentHistory(Long customerId, Pageable pageable) {
        log.debug("Fetching payment history for customer: {}", customerId);
        
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(customerId);
        }
        
        Page<Payment> payments = paymentRepository.findBySubscriptionCustomerId(customerId, pageable);
        return payments.map(this::mapToResponse);
    }
    
    /**
     * Get payment by ID.
     */
    public PaymentResponse getPaymentById(Long id) {
        log.debug("Fetching payment with id: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        
        return mapToResponse(payment);
    }
    
    /**
     * Get all payments for a subscription.
     */
    public List<PaymentResponse> getPaymentsBySubscription(Long subscriptionId) {
        log.debug("Fetching payments for subscription: {}", subscriptionId);
        
        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new SubscriptionNotFoundException(subscriptionId);
        }
        
        List<Payment> payments = paymentRepository.findBySubscriptionId(subscriptionId);
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get pending payments for a customer.
     */
    public List<PaymentResponse> getPendingPayments(Long customerId) {
        log.debug("Fetching pending payments for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        
        // Get all active/overdue subscriptions and create payment records for next payment
        List<Subscription> subscriptions = subscriptionRepository
                .findByCustomerIdAndStatusIn(customerId, 
                        List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.OVERDUE));
        
        return subscriptions.stream()
                .filter(sub -> sub.getNextPaymentDate() != null)
                .map(sub -> {
                    BigDecimal lateFee = BigDecimal.ZERO;
                    if (sub.getNextPaymentDate().isBefore(LocalDate.now())) {
                        int daysLate = (int) ChronoUnit.DAYS.between(
                                sub.getNextPaymentDate(), LocalDate.now());
                        lateFee = subscriptionService.applyLateFee(sub.getId(), daysLate);
                    }
                    
                    return PaymentResponse.builder()
                            .subscriptionId(sub.getId())
                            .customerName(customer.getFirstName() + " " + customer.getLastName())
                            .amount(sub.getPaymentPlan().getBaseAmount())
                            .dueDate(sub.getNextPaymentDate())
                            .status(PaymentStatus.PENDING)
                            .lateFee(lateFee)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Generate a payment link (mock implementation for MVP).
     */
    public String generatePaymentLink(Long subscriptionId) {
        log.info("Generating payment link for subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId));
        
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new InvalidPaymentException("Cannot generate payment link for cancelled subscription");
        }
        
        // Mock payment link - in real implementation, this would integrate with payment gateway
        String paymentLink = String.format("https://payment.example.com/pay/%s/%s",
                subscriptionId, UUID.randomUUID());
        
        log.info("Payment link generated: {}", paymentLink);
        return paymentLink;
    }
    
    /**
     * Calculate late fee based on days overdue.
     */
    public BigDecimal calculateLateFee(Subscription subscription) {
        log.debug("Calculating late fee for subscription: {}", subscription.getId());
        
        if (subscription.getNextPaymentDate() == null || 
            !subscription.getNextPaymentDate().isBefore(LocalDate.now())) {
            return BigDecimal.ZERO;
        }
        
        PaymentPlan plan = subscription.getPaymentPlan();
        int daysLate = (int) ChronoUnit.DAYS.between(subscription.getNextPaymentDate(), LocalDate.now());
        
        // Check if within grace period
        if (daysLate <= plan.getGracePeriodDays()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate late fee: (daysLate - gracePeriod) * lateFeePerDay
        int chargeableDays = daysLate - plan.getGracePeriodDays();
        BigDecimal lateFee = plan.getLateFeePerDay()
                .multiply(BigDecimal.valueOf(chargeableDays))
                .setScale(2, RoundingMode.HALF_UP);
        
        return lateFee;
    }

    // ==================== MOCK PAYMENT GATEWAY METHODS ====================

    /**
     * Initiate a payment through the mock gateway.
     * Creates a PENDING payment and returns a mock payment URL.
     */
    @Transactional
    public PaymentInitResponse initiatePayment(PaymentInitRequest request) {
        log.info("Initiating payment for subscription: {}", request.getSubscriptionId());
        
        try {
            // 1. Get subscription
            Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new SubscriptionNotFoundException(request.getSubscriptionId()));

            // 2. Validate subscription can accept payment
            if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
                return PaymentInitResponse.builder()
                        .success(false)
                        .errorMessage("اشتراک لغو شده است و نمی‌توان برای آن پرداخت انجام داد")
                        .build();
            }

            // 3. Validate amount (optional: check if exceeds remaining amount)
            // Note: In this context, we'll allow any positive amount

            // 4. Create payment record with PENDING status
            String transactionId = generateMockTransactionId();
            
            Payment payment = Payment.builder()
                    .subscription(subscription)
                    .amount(request.getAmount())
                    .status(PaymentStatus.PENDING)
                    .transactionId(transactionId)
                    .description(request.getDescription())
                    .createdAt(LocalDateTime.now())
                    .build();

            payment = paymentRepository.save(payment);
            log.info("Payment created with transaction ID: {}", transactionId);

            // 5. Generate mock payment URL
            String paymentUrl = String.format(
                    "http://localhost:8080/payment-gateway.html?transactionId=%s&amount=%s",
                    payment.getTransactionId(),
                    request.getAmount()
            );

            return PaymentInitResponse.builder()
                    .transactionId(payment.getTransactionId())
                    .paymentUrl(paymentUrl)
                    .success(true)
                    .build();

        } catch (SubscriptionNotFoundException e) {
            log.error("Subscription not found: {}", request.getSubscriptionId());
            return PaymentInitResponse.builder()
                    .success(false)
                    .errorMessage("اشتراک یافت نشد")
                    .build();
        } catch (Exception e) {
            log.error("Error initiating payment", e);
            return PaymentInitResponse.builder()
                    .success(false)
                    .errorMessage("خطا در ایجاد تراکنش پرداخت: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Verify and complete a payment from the mock gateway.
     * Updates payment status and subscription details.
     */
    @Transactional
    public PaymentVerifyResponse verifyMockPayment(String transactionId, boolean success) {
        log.info("Verifying payment with transaction ID: {} - Success: {}", transactionId, success);

        try {
            // 1. Find payment by transaction ID
            Payment payment = paymentRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new PaymentNotFoundException("تراکنش با شناسه " + transactionId + " یافت نشد"));

            // 2. Check if already processed
            if (payment.getStatus() != PaymentStatus.PENDING) {
                String statusMessage = payment.getStatus() == PaymentStatus.SUCCESS 
                        ? "این تراکنش قبلاً با موفقیت پردازش شده است" 
                        : "این تراکنش قبلاً پردازش شده است";
                
                return PaymentVerifyResponse.builder()
                        .transactionId(transactionId)
                        .success(payment.getStatus() == PaymentStatus.SUCCESS)
                        .message(statusMessage)
                        .build();
            }

            // 3. Update payment status based on result
            if (success) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(LocalDateTime.now());
                payment.setPaymentDate(LocalDateTime.now());

                // 4. Update subscription
                Subscription subscription = payment.getSubscription();
                subscription.setLastPaymentDate(LocalDate.now());
                
                // Calculate next payment date
                LocalDate nextPaymentDate = subscriptionService.calculateNextPaymentDate(subscription);
                subscription.setNextPaymentDate(nextPaymentDate);

                // Update subscription status if was OVERDUE or PENDING
                if (subscription.getStatus() == SubscriptionStatus.OVERDUE || 
                    subscription.getStatus() == SubscriptionStatus.PENDING) {
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                    log.info("Subscription {} status changed to ACTIVE", subscription.getId());
                }

                subscriptionRepository.save(subscription);
                log.info("Payment {} verified successfully", transactionId);

                return PaymentVerifyResponse.builder()
                        .transactionId(transactionId)
                        .success(true)
                        .message("پرداخت با موفقیت انجام شد")
                        .build();

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.info("Payment {} marked as failed", transactionId);

                return PaymentVerifyResponse.builder()
                        .transactionId(transactionId)
                        .success(false)
                        .message("پرداخت لغو شد یا با خطا مواجه شد")
                        .build();
            }

        } catch (PaymentNotFoundException e) {
            log.error("Payment not found: {}", transactionId);
            return PaymentVerifyResponse.builder()
                    .transactionId(transactionId)
                    .success(false)
                    .message("تراکنش یافت نشد")
                    .build();
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return PaymentVerifyResponse.builder()
                    .transactionId(transactionId)
                    .success(false)
                    .message("خطا در تأیید پرداخت: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get payment details for the payment gateway page.
     * Returns customer, plan, and payment information.
     */
    public PaymentDetailsDto getPaymentDetails(String transactionId) {
        log.info("Fetching payment details for transaction: {}", transactionId);

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("تراکنش با شناسه " + transactionId + " یافت نشد"));

        Subscription subscription = payment.getSubscription();
        Customer customer = subscription.getCustomer();
        PaymentPlan plan = subscription.getPaymentPlan();

        // Build customer info
        PaymentDetailsDto.CustomerInfo customerInfo = PaymentDetailsDto.CustomerInfo.builder()
                .id(customer.getId())
                .fullName(customer.getFirstName() + " " + customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhoneNumber())
                .build();

        // Build plan info
        PaymentDetailsDto.PlanInfo planInfo = PaymentDetailsDto.PlanInfo.builder()
                .id(plan.getId())
                .name(plan.getPlanName())
                .duration(plan.getPeriodCount())
                .build();

        // Build payment details
        return PaymentDetailsDto.builder()
                .transactionId(transactionId)
                .amount(payment.getAmount())
                .customer(customerInfo)
                .plan(planInfo)
                .build();
    }

    /**
     * Get payment history for a business with optional filters.
     */
    public List<PaymentResponse> getPaymentHistory(Long businessId, LocalDate startDate,
                                                   LocalDate endDate, PaymentStatus status) {
        log.debug("Fetching payment history for business: {}", businessId);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        List<Payment> payments = paymentRepository.findByBusinessIdAndFilters(
                businessId, startDateTime, endDateTime, status);

        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // ==================== END MOCK PAYMENT GATEWAY METHODS ====================
    
    /**
     * Update subscription after successful payment.
     */
    private void updateSubscriptionAfterPayment(Subscription subscription) {
        log.debug("Updating subscription after payment: {}", subscription.getId());
        
        // Update last payment date
        subscription.setLastPaymentDate(LocalDate.now());
        
        // Calculate next payment date
        LocalDate nextPaymentDate = subscriptionService.calculateNextPaymentDate(subscription);
        subscription.setNextPaymentDate(nextPaymentDate);
        
        // Update status to ACTIVE if was OVERDUE
        if (subscription.getStatus() == SubscriptionStatus.OVERDUE) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            log.info("Subscription {} status changed from OVERDUE to ACTIVE", subscription.getId());
        }
        
        subscriptionRepository.save(subscription);
    }
    
    /**
     * Generate a unique transaction ID.
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate a unique transaction ID for mock payment gateway.
     */
    private String generateMockTransactionId() {
        return "TXN" + System.currentTimeMillis() +
                String.format("%04d", new java.util.Random().nextInt(10000));
    }
    
    /**
     * Map Payment entity to PaymentResponse DTO.
     */
    private PaymentResponse mapToResponse(Payment payment) {
        String customerName = payment.getSubscription().getCustomer().getFirstName() + " " +
                payment.getSubscription().getCustomer().getLastName();
        
        return PaymentResponse.builder()
                .id(payment.getId())
                .subscriptionId(payment.getSubscription().getId())
                .customerName(customerName)
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .dueDate(payment.getDueDate())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .lateFee(payment.getLateFee())
                .notes(payment.getNotes())
                .build();
    }
}
