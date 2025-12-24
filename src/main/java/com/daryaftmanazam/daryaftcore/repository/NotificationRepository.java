package com.daryaftmanazam.daryaftcore.repository;

import com.daryaftmanazam.daryaftcore.model.Notification;
import com.daryaftmanazam.daryaftcore.model.Notification.NotificationStatus;
import com.daryaftmanazam.daryaftcore.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by customer ID.
     *
     * @param customerId the customer ID
     * @return List of notifications for the specified customer
     */
    List<Notification> findByCustomerId(Long customerId);

    /**
     * Find pending notifications.
     *
     * @return List of pending notifications
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Find pending notifications (status = PENDING).
     *
     * @return List of pending notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING'")
    List<Notification> findPendingNotifications();

    /**
     * Find notifications by notification type and status.
     *
     * @param notificationType the notification type
     * @param status           the notification status
     * @return List of notifications matching the criteria
     */
    List<Notification> findByNotificationTypeAndStatus(NotificationType notificationType, 
                                                        NotificationStatus status);

    /**
     * Find notifications by customer ID and status.
     *
     * @param customerId the customer ID
     * @param status     the notification status
     * @return List of notifications matching the criteria
     */
    List<Notification> findByCustomerIdAndStatus(Long customerId, NotificationStatus status);

    /**
     * Find pending notifications by customer ID.
     *
     * @param customerId the customer ID
     * @return List of pending notifications for the specified customer
     */
    @Query("SELECT n FROM Notification n WHERE n.customer.id = :customerId AND n.status = 'PENDING'")
    List<Notification> findPendingNotificationsByCustomer(@Param("customerId") Long customerId);

    /**
     * Find notifications by subscription ID.
     *
     * @param subscriptionId the subscription ID
     * @return List of notifications for the specified subscription
     */
    List<Notification> findBySubscriptionId(Long subscriptionId);

    /**
     * Find notifications by notification type.
     *
     * @param notificationType the notification type
     * @return List of notifications of the specified type
     */
    List<Notification> findByNotificationType(NotificationType notificationType);

    /**
     * Find failed notifications.
     *
     * @return List of failed notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED'")
    List<Notification> findFailedNotifications();

    /**
     * Find sent notifications within a date range.
     *
     * @param startDate the start date and time
     * @param endDate   the end date and time
     * @return List of sent notifications within the specified date range
     */
    @Query("SELECT n FROM Notification n WHERE n.sentAt BETWEEN :startDate AND :endDate AND n.status = 'SENT'")
    List<Notification> findSentNotificationsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find notifications by business ID (through customer relationship).
     *
     * @param businessId the business ID
     * @return List of notifications for customers of the specified business
     */
    @Query("SELECT n FROM Notification n WHERE n.customer.business.id = :businessId")
    List<Notification> findByBusinessId(@Param("businessId") Long businessId);

    /**
     * Find pending notifications by business ID.
     *
     * @param businessId the business ID
     * @return List of pending notifications for the specified business
     */
    @Query("SELECT n FROM Notification n WHERE n.customer.business.id = :businessId AND n.status = 'PENDING'")
    List<Notification> findPendingNotificationsByBusiness(@Param("businessId") Long businessId);

    /**
     * Count notifications by customer ID.
     *
     * @param customerId the customer ID
     * @return Count of notifications for the specified customer
     */
    long countByCustomerId(Long customerId);

    /**
     * Count pending notifications by customer ID.
     *
     * @param customerId the customer ID
     * @return Count of pending notifications for the specified customer
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.customer.id = :customerId AND n.status = 'PENDING'")
    long countPendingNotificationsByCustomer(@Param("customerId") Long customerId);

    /**
     * Count failed notifications.
     *
     * @return Count of failed notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'FAILED'")
    long countFailedNotifications();

    /**
     * Find notifications by customer ID and notification type.
     *
     * @param customerId       the customer ID
     * @param notificationType the notification type
     * @return List of notifications matching the criteria
     */
    List<Notification> findByCustomerIdAndNotificationType(Long customerId, 
                                                            NotificationType notificationType);

    /**
     * Delete old sent notifications before a specific date.
     *
     * @param beforeDate the date threshold
     */
    @Query("DELETE FROM Notification n WHERE n.status = 'SENT' AND n.sentAt < :beforeDate")
    void deleteOldSentNotifications(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * Check if a notification exists by customer, subscription, type and sent after a date.
     *
     * @param customerId the customer ID
     * @param subscriptionId the subscription ID
     * @param notificationType the notification type
     * @param sentAt the date to check after
     * @return true if notification exists
     */
    boolean existsByCustomerIdAndSubscriptionIdAndNotificationTypeAndSentAtAfter(
            Long customerId, Long subscriptionId, NotificationType notificationType, LocalDateTime sentAt);
    
    /**
     * Find notifications by customer ID, subscription ID and sent after a specific date.
     *
     * @param customerId the customer ID
     * @param subscriptionId the subscription ID
     * @param sentAt the date to check after
     * @return List of notifications matching the criteria
     */
    List<Notification> findByCustomerIdAndSubscriptionIdAndSentAtAfter(
            Long customerId, Long subscriptionId, LocalDateTime sentAt);
}
