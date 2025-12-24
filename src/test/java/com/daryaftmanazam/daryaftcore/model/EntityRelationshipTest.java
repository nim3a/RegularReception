package com.daryaftmanazam.daryaftcore.model;

import com.daryaftmanazam.daryaftcore.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify entity relationships work correctly.
 */
class EntityRelationshipTest {

    private Business business;
    private Customer customer;
    private PaymentPlan paymentPlan;
    private Subscription subscription;
    private Payment payment;
    private Notification notification;

    @BeforeEach
    void setUp() {
        // Create Business
        business = Business.builder()
                .businessName("Test Gym")
                .ownerName("John Doe")
                .contactEmail("john@testgym.com")
                .contactPhone("09123456789")
                .description("A test fitness center")
                .isActive(true)
                .build();

        // Create Customer
        customer = Customer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("09187654321")
                .email("jane@example.com")
                .customerType(CustomerType.REGULAR)
                .isActive(true)
                .joinDate(LocalDate.now())
                .build();

        // Create PaymentPlan
        paymentPlan = PaymentPlan.builder()
                .planName("Monthly Membership")
                .periodType(PeriodType.MONTHLY)
                .periodCount(1)
                .baseAmount(BigDecimal.valueOf(500000))
                .discountPercentage(BigDecimal.ZERO)
                .lateFeePerDay(BigDecimal.valueOf(10000))
                .gracePeriodDays(3)
                .isActive(true)
                .business(business)
                .build();

        // Create Subscription
        subscription = Subscription.builder()
                .paymentPlan(paymentPlan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status(SubscriptionStatus.ACTIVE)
                .totalAmount(BigDecimal.valueOf(500000))
                .discountApplied(BigDecimal.ZERO)
                .nextPaymentDate(LocalDate.now().plusMonths(1))
                .build();

        // Create Payment
        payment = Payment.builder()
                .amount(BigDecimal.valueOf(500000))
                .paymentDate(LocalDateTime.now())
                .dueDate(LocalDate.now())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod("Cash")
                .transactionId("TXN-001")
                .lateFee(BigDecimal.ZERO)
                .notes("First payment")
                .build();

        // Create Notification
        notification = Notification.builder()
                .notificationType(NotificationType.CONFIRMATION)
                .channel(Channel.SMS)
                .message("Payment received successfully")
                .sentAt(LocalDateTime.now())
                .status(Notification.NotificationStatus.SENT)
                .build();
    }

    @Test
    void testBusinessCustomerRelationship() {
        // Add customer to business
        business.addCustomer(customer);

        // Verify relationship
        assertEquals(1, business.getCustomers().size());
        assertTrue(business.getCustomers().contains(customer));
        assertEquals(business, customer.getBusiness());

        // Remove customer from business
        business.removeCustomer(customer);
        assertEquals(0, business.getCustomers().size());
        assertNull(customer.getBusiness());
    }

    @Test
    void testCustomerSubscriptionRelationship() {
        // Set up business-customer relationship first
        business.addCustomer(customer);

        // Add subscription to customer
        customer.addSubscription(subscription);

        // Verify relationship
        assertEquals(1, customer.getSubscriptions().size());
        assertTrue(customer.getSubscriptions().contains(subscription));
        assertEquals(customer, subscription.getCustomer());

        // Remove subscription from customer
        customer.removeSubscription(subscription);
        assertEquals(0, customer.getSubscriptions().size());
        assertNull(subscription.getCustomer());
    }

    @Test
    void testSubscriptionPaymentRelationship() {
        // Add payment to subscription
        subscription.addPayment(payment);

        // Verify relationship
        assertEquals(1, subscription.getPayments().size());
        assertTrue(subscription.getPayments().contains(payment));
        assertEquals(subscription, payment.getSubscription());

        // Remove payment from subscription
        subscription.removePayment(payment);
        assertEquals(0, subscription.getPayments().size());
        assertNull(payment.getSubscription());
    }

    @Test
    void testPaymentPlanBusinessRelationship() {
        // Verify relationship
        assertEquals(business, paymentPlan.getBusiness());
    }

    @Test
    void testNotificationCustomerRelationship() {
        // Set notification customer
        notification.setCustomer(customer);

        // Verify relationship
        assertEquals(customer, notification.getCustomer());
    }

    @Test
    void testNotificationSubscriptionRelationship() {
        // Set notification subscription
        notification.setSubscription(subscription);

        // Verify relationship
        assertEquals(subscription, notification.getSubscription());
    }

    @Test
    void testCompleteEntityGraph() {
        // Build complete entity graph
        business.addCustomer(customer);
        customer.addSubscription(subscription);
        subscription.addPayment(payment);
        notification.setCustomer(customer);
        notification.setSubscription(subscription);

        // Verify complete graph
        assertNotNull(business.getCustomers());
        assertEquals(1, business.getCustomers().size());
        
        Customer retrievedCustomer = business.getCustomers().get(0);
        assertNotNull(retrievedCustomer.getSubscriptions());
        assertEquals(1, retrievedCustomer.getSubscriptions().size());
        
        Subscription retrievedSubscription = retrievedCustomer.getSubscriptions().get(0);
        assertNotNull(retrievedSubscription.getPayments());
        assertEquals(1, retrievedSubscription.getPayments().size());
        
        assertEquals(customer, notification.getCustomer());
        assertEquals(subscription, notification.getSubscription());
    }

    @Test
    void testBusinessValidation() {
        // Test business name
        assertNotNull(business.getBusinessName());
        assertEquals("Test Gym", business.getBusinessName());

        // Test owner name
        assertNotNull(business.getOwnerName());
        assertEquals("John Doe", business.getOwnerName());

        // Test phone format
        assertTrue(business.getContactPhone().matches("^09\\d{9}$"));

        // Test default values
        assertTrue(business.getIsActive());
    }

    @Test
    void testCustomerEnums() {
        assertEquals(CustomerType.REGULAR, customer.getCustomerType());
        
        // Test all customer types
        assertDoesNotThrow(() -> {
            customer.setCustomerType(CustomerType.NEW);
            customer.setCustomerType(CustomerType.VIP);
        });
    }

    @Test
    void testPaymentPlanEnums() {
        assertEquals(PeriodType.MONTHLY, paymentPlan.getPeriodType());
        
        // Test all period types
        assertDoesNotThrow(() -> {
            paymentPlan.setPeriodType(PeriodType.WEEKLY);
            paymentPlan.setPeriodType(PeriodType.QUARTERLY);
            paymentPlan.setPeriodType(PeriodType.SEMI_ANNUAL);
        });
    }

    @Test
    void testSubscriptionStatus() {
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        
        // Test all subscription statuses
        assertDoesNotThrow(() -> {
            subscription.setStatus(SubscriptionStatus.PENDING);
            subscription.setStatus(SubscriptionStatus.OVERDUE);
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setStatus(SubscriptionStatus.EXPIRED);
        });
    }

    @Test
    void testPaymentStatus() {
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        
        // Test all payment statuses
        assertDoesNotThrow(() -> {
            payment.setStatus(PaymentStatus.PENDING);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setStatus(PaymentStatus.REFUNDED);
        });
    }

    @Test
    void testNotificationEnums() {
        assertEquals(NotificationType.CONFIRMATION, notification.getNotificationType());
        assertEquals(Channel.SMS, notification.getChannel());
        assertEquals(Notification.NotificationStatus.SENT, notification.getStatus());
        
        // Test all notification types
        assertDoesNotThrow(() -> {
            notification.setNotificationType(NotificationType.REMINDER);
            notification.setNotificationType(NotificationType.OVERDUE);
            notification.setNotificationType(NotificationType.WARNING);
        });
        
        // Test all channels
        assertDoesNotThrow(() -> {
            notification.setChannel(Channel.EMAIL);
            notification.setChannel(Channel.TELEGRAM);
            notification.setChannel(Channel.PUSH);
        });
    }

    @Test
    void testBigDecimalFields() {
        // Test payment plan amounts
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(paymentPlan.getBaseAmount()));
        assertEquals(0, BigDecimal.valueOf(10000).compareTo(paymentPlan.getLateFeePerDay()));

        // Test subscription amounts
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(subscription.getTotalAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(subscription.getDiscountApplied()));

        // Test payment amounts
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(payment.getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(payment.getLateFee()));
    }
}
