package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.BaseIntegrationTest;
import com.daryaftmanazam.daryaftcore.dto.request.PaymentInitRequest;
import com.daryaftmanazam.daryaftcore.model.*;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentInterval;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController
 * Tests payment initiation, verification, and history
 */
@DisplayName("Payment Controller Integration Tests")
class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;

    private Business testBusiness;
    private Customer testCustomer;
    private PaymentPlan testPlan;
    private Subscription testSubscription;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clean up
        paymentRepository.deleteAll();
        subscriptionRepository.deleteAll();
        customerRepository.deleteAll();
        paymentPlanRepository.deleteAll();
        businessRepository.deleteAll();

        // Create test business
        testBusiness = new Business();
        testBusiness.setName("Test Business");
        testBusiness.setOwnerName("Test Owner");
        testBusiness.setPhoneNumber("09121111111");
        testBusiness.setAddress("Test Address");
        testBusiness = businessRepository.save(testBusiness);

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setFirstName("رضا");
        testCustomer.setLastName("احمدی");
        testCustomer.setPhoneNumber("09123456789");
        testCustomer.setBusinessId(testBusiness.getId());
        testCustomer = customerRepository.save(testCustomer);

        // Create test payment plan
        testPlan = new PaymentPlan();
        testPlan.setName("ماهانه");
        testPlan.setDescription("پلن ماهانه");
        testPlan.setAmount(BigDecimal.valueOf(500000));
        testPlan.setPaymentInterval(PaymentInterval.MONTHLY);
        testPlan.setBusinessId(testBusiness.getId());
        testPlan = paymentPlanRepository.save(testPlan);

        // Create test subscription
        testSubscription = new Subscription();
        testSubscription.setCustomer(testCustomer);
        testSubscription.setPaymentPlan(testPlan);
        testSubscription.setBusinessId(testBusiness.getId());
        testSubscription.setStartDate(LocalDate.now());
        testSubscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        testSubscription.setStatus(SubscriptionStatus.ACTIVE);
        testSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        testSubscription = subscriptionRepository.save(testSubscription);

        // Generate auth token
        authToken = generateTestToken("testuser", testBusiness.getId(), "BUSINESS_ADMIN");
    }

    @Test
    @DisplayName("Should initiate payment successfully")
    void testInitiatePayment_Success() throws Exception {
        PaymentInitRequest request = new PaymentInitRequest();
        request.setSubscriptionId(testSubscription.getId());
        request.setAmount(BigDecimal.valueOf(500000));
        request.setDescription("پرداخت اشتراک ماهانه");

        mockMvc.perform(post("/api/payments/initiate")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentUrl").exists())
                .andExpect(jsonPath("$.data.trackingCode").exists());
    }

    @Test
    @DisplayName("Should verify successful payment")
    void testVerifyPayment_Success() throws Exception {
        // Create a pending payment
        Payment payment = new Payment();
        payment.setSubscription(testSubscription);
        payment.setBusinessId(testBusiness.getId());
        payment.setAmount(BigDecimal.valueOf(500000));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTrackingCode("TEST-TRACK-123");
        payment.setPaymentDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // Simulate payment gateway callback
        mockMvc.perform(get("/api/payments/verify")
                        .param("trackingCode", "TEST-TRACK-123")
                        .param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should handle failed payment verification")
    void testVerifyPayment_Failed() throws Exception {
        // Create a pending payment
        Payment payment = new Payment();
        payment.setSubscription(testSubscription);
        payment.setBusinessId(testBusiness.getId());
        payment.setAmount(BigDecimal.valueOf(500000));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTrackingCode("TEST-TRACK-456");
        payment.setPaymentDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/verify")
                        .param("trackingCode", "TEST-TRACK-456")
                        .param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"));
    }

    @Test
    @DisplayName("Should get payment history for subscription")
    void testGetPaymentHistory_ForSubscription() throws Exception {
        // Create multiple payments
        for (int i = 0; i < 3; i++) {
            Payment payment = new Payment();
            payment.setSubscription(testSubscription);
            payment.setBusinessId(testBusiness.getId());
            payment.setAmount(BigDecimal.valueOf(500000));
            payment.setStatus(i == 0 ? PaymentStatus.COMPLETED : PaymentStatus.PENDING);
            payment.setTrackingCode("TRACK-" + i);
            payment.setPaymentDate(LocalDateTime.now().minusDays(i));
            paymentRepository.save(payment);
        }

        mockMvc.perform(get("/api/payments/subscription/" + testSubscription.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("Should get payment history for customer")
    void testGetPaymentHistory_ForCustomer() throws Exception {
        // Create payment
        Payment payment = new Payment();
        payment.setSubscription(testSubscription);
        payment.setBusinessId(testBusiness.getId());
        payment.setAmount(BigDecimal.valueOf(500000));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTrackingCode("CUST-TRACK-001");
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/customer/" + testCustomer.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should get payment by ID")
    void testGetPaymentById_Success() throws Exception {
        Payment payment = new Payment();
        payment.setSubscription(testSubscription);
        payment.setBusinessId(testBusiness.getId());
        payment.setAmount(BigDecimal.valueOf(500000));
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTrackingCode("GET-TRACK-001");
        payment.setPaymentDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/" + payment.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(payment.getId()))
                .andExpect(jsonPath("$.data.amount").value(500000))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("Should get payment statistics for business")
    void testGetPaymentStatistics() throws Exception {
        // Create completed payments
        for (int i = 0; i < 5; i++) {
            Payment payment = new Payment();
            payment.setSubscription(testSubscription);
            payment.setBusinessId(testBusiness.getId());
            payment.setAmount(BigDecimal.valueOf(500000));
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTrackingCode("STAT-TRACK-" + i);
            payment.setPaymentDate(LocalDateTime.now().minusDays(i));
            paymentRepository.save(payment);
        }

        mockMvc.perform(get("/api/payments/business/" + testBusiness.getId() + "/statistics")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPayments").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.data.totalAmount").value(greaterThanOrEqualTo(2500000)));
    }

    @Test
    @DisplayName("Should filter payments by status")
    void testGetPayments_FilterByStatus() throws Exception {
        // Create payments with different statuses
        Payment completedPayment = new Payment();
        completedPayment.setSubscription(testSubscription);
        completedPayment.setBusinessId(testBusiness.getId());
        completedPayment.setAmount(BigDecimal.valueOf(500000));
        completedPayment.setStatus(PaymentStatus.COMPLETED);
        completedPayment.setTrackingCode("COMPLETED-001");
        completedPayment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(completedPayment);

        Payment pendingPayment = new Payment();
        pendingPayment.setSubscription(testSubscription);
        pendingPayment.setBusinessId(testBusiness.getId());
        pendingPayment.setAmount(BigDecimal.valueOf(500000));
        pendingPayment.setStatus(PaymentStatus.PENDING);
        pendingPayment.setTrackingCode("PENDING-001");
        pendingPayment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(pendingPayment);

        mockMvc.perform(get("/api/payments/business/" + testBusiness.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[?(@.status=='COMPLETED')]").exists());
    }

    @Test
    @DisplayName("Should enforce multi-tenant isolation")
    void testGetPayments_MultiTenantIsolation() throws Exception {
        // Create another business
        Business otherBusiness = new Business();
        otherBusiness.setName("Other Business");
        otherBusiness.setOwnerName("Other Owner");
        otherBusiness.setPhoneNumber("09129999999");
        otherBusiness.setAddress("Other Address");
        otherBusiness = businessRepository.save(otherBusiness);

        // Create payment for other business
        Payment otherPayment = new Payment();
        otherPayment.setSubscription(testSubscription);
        otherPayment.setBusinessId(otherBusiness.getId());
        otherPayment.setAmount(BigDecimal.valueOf(500000));
        otherPayment.setStatus(PaymentStatus.COMPLETED);
        otherPayment.setTrackingCode("OTHER-TRACK-001");
        otherPayment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(otherPayment);

        // Request with testBusiness token should not see otherBusiness payment
        mockMvc.perform(get("/api/payments/business/" + testBusiness.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 401 without authentication")
    void testInitiatePayment_WithoutAuth_Unauthorized() throws Exception {
        PaymentInitRequest request = new PaymentInitRequest();
        request.setSubscriptionId(testSubscription.getId());
        request.setAmount(BigDecimal.valueOf(500000));

        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
