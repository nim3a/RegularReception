package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Subscription;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Subscription entity operations.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find subscriptions by customer ID.
     *
     * @param customerId the customer ID
     * @return List of subscriptions for the specified customer
     */
    List<Subscription> findByCustomerId(Long customerId);

    /**
     * Find subscriptions by status.
     *
     * @param status the subscription status
     * @return List of subscriptions with the specified status
     */
    List<Subscription> findByStatus(SubscriptionStatus status);

    /**
     * Find all active subscriptions (ACTIVE status).
     *
     * @return List of active subscriptions
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE'")
    List<Subscription> findActiveSubscriptions();

    /**
     * Find overdue subscriptions (status=OVERDUE or nextPaymentDate < today).
     *
     * @param today the current date
     * @return List of overdue subscriptions
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'OVERDUE' OR " +
           "(s.status = 'ACTIVE' AND s.nextPaymentDate < :today)")
    List<Subscription> findOverdueSubscriptions(@Param("today") LocalDate today);

    /**
     * Find active subscriptions ending on a specific date.
     *
     * @param date the end date
     * @return List of active subscriptions ending on the specified date
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate = :date")
    List<Subscription> findActiveSubscriptionsEndingOn(@Param("date") LocalDate date);

    /**
     * Find active subscriptions ending on a specific date for a business.
     *
     * @param date the end date
     * @param businessId the business ID
     * @return List of active subscriptions ending on the specified date
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.endDate = :date AND s.customer.business.id = :businessId")
    List<Subscription> findActiveSubscriptionsEndingOn(@Param("date") LocalDate date, 
                                                       @Param("businessId") Long businessId);

    /**
     * Find subscriptions expiring soon (endDate within next N days).
     *
     * @param startDate the start date (today)
     * @param endDate   the end date (today + N days)
     * @return List of subscriptions expiring within the specified period
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND " +
           "s.endDate BETWEEN :startDate AND :endDate")
    List<Subscription> findExpiringSoon(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * Find subscriptions by business ID (through customer relationship).
     *
     * @param businessId the business ID
     * @return List of subscriptions for customers of the specified business
     */
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId")
    List<Subscription> findByBusinessId(@Param("businessId") Long businessId);

    /**
     * Find active subscriptions by business ID.
     *
     * @param businessId the business ID
     * @return List of active subscriptions for the specified business
     */
    @Query("SELECT s FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = 'ACTIVE'")
    List<Subscription> findActiveSubscriptionsByBusinessId(@Param("businessId") Long businessId);

    /**
     * Find subscriptions by customer ID and status.
     *
     * @param customerId the customer ID
     * @param status     the subscription status
     * @return List of subscriptions matching the criteria
     */
    List<Subscription> findByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);

    /**
     * Find subscriptions by payment plan ID.
     *
     * @param paymentPlanId the payment plan ID
     * @return List of subscriptions using the specified payment plan
     */
    List<Subscription> findByPaymentPlanId(Long paymentPlanId);

    /**
     * Count subscriptions by customer ID.
     *
     * @param customerId the customer ID
     * @return Count of subscriptions for the specified customer
     */
    long countByCustomerId(Long customerId);

    /**
     * Count subscriptions by status.
     *
     * @param status the subscription status
     * @return Count of subscriptions with the specified status
     */
    long countByStatus(SubscriptionStatus status);

    /**
     * Count active subscriptions by customer ID.
     *
     * @param customerId the customer ID
     * @return Count of active subscriptions for the specified customer
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.id = :customerId AND s.status = 'ACTIVE'")
    long countActiveSubscriptionsByCustomerId(@Param("customerId") Long customerId);

    /**
     * Count subscriptions by business ID.
     *
     * @param businessId the business ID
     * @return Count of subscriptions for the specified business
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.business.id = :businessId")
    long countByBusinessId(@Param("businessId") Long businessId);

    /**
     * Find subscriptions with next payment date before a specified date.
     *
     * @param date the date to compare against
     * @return List of subscriptions with next payment date before the specified date
     */
    @Query("SELECT s FROM Subscription s WHERE s.nextPaymentDate < :date AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsWithPaymentDueBefore(@Param("date") LocalDate date);

    /**
     * Find expired subscriptions that are still marked as active.
     *
     * @param today the current date
     * @return List of subscriptions that should be expired
     */
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :today AND s.status != 'EXPIRED'")
    List<Subscription> findExpiredButNotMarked(@Param("today") LocalDate today);
    
    /**
     * Count subscriptions by business ID and status.
     *
     * @param businessId the business ID
     * @param status the subscription status
     * @return Count of subscriptions matching the criteria
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.customer.business.id = :businessId AND s.status = :status")
    int countByCustomerBusinessIdAndStatus(@Param("businessId") Long businessId, 
                                           @Param("status") SubscriptionStatus status);
    
    /**
     * Find subscriptions by customer ID ordered by created date descending.
     *
     * @param customerId the customer ID
     * @return List of subscriptions ordered by creation date
     */
    List<Subscription> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    /**
     * Find subscriptions by status and next payment date.
     *
     * @param status the subscription status
     * @param nextPaymentDate the next payment date
     * @return List of subscriptions matching the criteria
     */
    List<Subscription> findByStatusAndNextPaymentDate(SubscriptionStatus status, LocalDate nextPaymentDate);
    
    /**
     * Find subscriptions by customer ID and status list.
     *
     * @param customerId the customer ID
     * @param statuses list of subscription statuses
     * @return List of subscriptions matching the criteria
     */
    List<Subscription> findByCustomerIdAndStatusIn(Long customerId, List<SubscriptionStatus> statuses);
    
    /**
     * Sum pending payments amount by business ID.
     *
     * @param businessId the business ID
     * @return Sum of pending payments
     */
    @Query("SELECT SUM(s.totalAmount) FROM Subscription s WHERE s.customer.business.id = :businessId " +
           "AND s.status IN ('ACTIVE', 'OVERDUE')")
    BigDecimal sumPendingPaymentsByBusinessId(@Param("businessId") Long businessId);
    
    /**
     * Sum overdue amount by business ID.
     *
     * @param businessId the business ID
     * @return Sum of overdue amounts
     */
    @Query("SELECT SUM(s.totalAmount) FROM Subscription s WHERE s.customer.business.id = :businessId " +
           "AND s.status = 'OVERDUE'")
    BigDecimal sumOverdueAmountByBusinessId(@Param("businessId") Long businessId);
}
