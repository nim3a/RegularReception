package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Payment;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by subscription ID.
     *
     * @param subscriptionId the subscription ID
     * @return List of payments for the specified subscription
     */
    List<Payment> findBySubscriptionId(Long subscriptionId);

    /**
     * Find payments by customer ID (through subscription relationship).
     *
     * @param customerId the customer ID
     * @return List of payments for the specified customer
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription.customer.id = :customerId")
    List<Payment> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find payments by status.
     *
     * @param status the payment status
     * @return List of payments with the specified status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find pending payments.
     *
     * @return List of pending payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING'")
    List<Payment> findPendingPayments();

    /**
     * Find payments by date range.
     *
     * @param startDate the start date and time
     * @param endDate   the end date and time
     * @return List of payments within the specified date range
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total payments by customer ID.
     *
     * @param customerId the customer ID
     * @return Total payment amount for the specified customer
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE " +
           "p.subscription.customer.id = :customerId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsByCustomer(@Param("customerId") Long customerId);

    /**
     * Calculate total payments by subscription ID.
     *
     * @param subscriptionId the subscription ID
     * @return Total payment amount for the specified subscription
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE " +
           "p.subscription.id = :subscriptionId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalPaymentsBySubscription(@Param("subscriptionId") Long subscriptionId);

    /**
     * Find a payment by transaction ID.
     *
     * @param transactionId the transaction ID
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find payments by subscription ID and status.
     *
     * @param subscriptionId the subscription ID
     * @param status         the payment status
     * @return List of payments matching the criteria
     */
    List<Payment> findBySubscriptionIdAndStatus(Long subscriptionId, PaymentStatus status);

    /**
     * Find pending payments by subscription ID.
     *
     * @param subscriptionId the subscription ID
     * @return List of pending payments for the specified subscription
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId AND p.status = 'PENDING'")
    List<Payment> findPendingPaymentsBySubscription(@Param("subscriptionId") Long subscriptionId);

    /**
     * Find failed payments by customer ID.
     *
     * @param customerId the customer ID
     * @return List of failed payments for the specified customer
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription.customer.id = :customerId AND p.status = 'FAILED'")
    List<Payment> findFailedPaymentsByCustomer(@Param("customerId") Long customerId);

    /**
     * Count payments by subscription ID.
     *
     * @param subscriptionId the subscription ID
     * @return Count of payments for the specified subscription
     */
    long countBySubscriptionId(Long subscriptionId);

    /**
     * Count completed payments by customer ID.
     *
     * @param customerId the customer ID
     * @return Count of completed payments for the specified customer
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.subscription.customer.id = :customerId AND p.status = 'COMPLETED'")
    long countCompletedPaymentsByCustomer(@Param("customerId") Long customerId);

    /**
     * Find payments by business ID (through subscription and customer relationships).
     *
     * @param businessId the business ID
     * @return List of payments for the specified business
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId")
    List<Payment> findByBusinessId(@Param("businessId") Long businessId);

    /**
     * Calculate total revenue by business ID.
     *
     * @param businessId the business ID
     * @return Total revenue for the specified business
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE " +
           "p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenueByBusiness(@Param("businessId") Long businessId);

    /**
     * Find payments with late fees.
     *
     * @return List of payments that have late fees applied
     */
    @Query("SELECT p FROM Payment p WHERE p.lateFee > 0")
    List<Payment> findPaymentsWithLateFees();

    /**
     * Find overdue payments (pending and past due date).
     *
     * @param currentDate the current date
     * @return List of overdue payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.dueDate < :currentDate")
    List<Payment> findOverduePayments(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Find payments by subscription customer ID with pagination.
     *
     * @param customerId the customer ID
     * @param pageable pagination information
     * @return Page of payments
     */
    Page<Payment> findBySubscriptionCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Sum amount by business ID for completed payments.
     *
     * @param businessId the business ID
     * @return Sum of completed payment amounts
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE " +
           "p.subscription.customer.business.id = :businessId AND p.status = 'COMPLETED'")
    BigDecimal sumAmountByBusinessId(@Param("businessId") Long businessId);

    /**
     * Find payments by business ID with optional filters.
     *
     * @param businessId the business ID
     * @param startDate optional start date filter
     * @param endDate optional end date filter
     * @param status optional status filter
     * @return List of payments matching the filters
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription.customer.business.id = :businessId " +
           "AND (:startDate IS NULL OR p.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR p.createdAt <= :endDate) " +
           "AND (:status IS NULL OR p.status = :status)")
    List<Payment> findByBusinessIdAndFilters(
        @Param("businessId") Long businessId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("status") PaymentStatus status
    );
}
