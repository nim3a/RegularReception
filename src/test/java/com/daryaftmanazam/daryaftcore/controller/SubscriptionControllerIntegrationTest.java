package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.BaseIntegrationTest;
import com.daryaftmanazam.daryaftcore.dto.request.SubscriptionRequest;
import com.daryaftmanazam.daryaftcore.model.*;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentInterval;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SubscriptionController
 * Tests subscription lifecycle, overdue detection, and renewal
 */
@DisplayName("Subscription Controller Integration Tests")
class SubscriptionControllerIntegrationTest extends BaseIntegrationTest {

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
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clean up
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
        testCustomer.setFirstName("علی");
        testCustomer.setLastName("محمدی");
        testCustomer.setPhoneNumber("09123456789");
        testCustomer.setBusinessId(testBusiness.getId());
        testCustomer = customerRepository.save(testCustomer);

        // Create test payment plan
        testPlan = new PaymentPlan();
        testPlan.setName("ماهانه استاندارد");
        testPlan.setDescription("پلن ماهانه");
        testPlan.setAmount(BigDecimal.valueOf(500000));
        testPlan.setPaymentInterval(PaymentInterval.MONTHLY);
        testPlan.setLateFeePercentage(BigDecimal.valueOf(2));
        testPlan.setBusinessId(testBusiness.getId());
        testPlan = paymentPlanRepository.save(testPlan);

        // Generate auth token
        authToken = generateTestToken("testuser", testBusiness.getId(), "BUSINESS_ADMIN");
    }

    @Test
    @DisplayName("Should create subscription successfully")
    void testCreateSubscription_Success() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCustomerId(testCustomer.getId());
        request.setPaymentPlanId(testPlan.getId());
        request.setStartDate(LocalDate.now());
        request.setNotes("تست اشتراک");

        mockMvc.perform(post("/api/subscriptions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.data.paymentPlanId").value(testPlan.getId()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.totalAmount").value(500000));
    }

    @Test
    @DisplayName("Should get subscription by ID")
    void testGetSubscriptionById_Success() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setCustomer(testCustomer);
        subscription.setPaymentPlan(testPlan);
        subscription.setBusinessId(testBusiness.getId());
        subscription.setStartDate(LocalDate.now());
        subscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscription = subscriptionRepository.save(subscription);

        mockMvc.perform(get("/api/subscriptions/" + subscription.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(subscription.getId()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should get overdue subscriptions")
    void testGetOverdueSubscriptions() throws Exception {
        // Create overdue subscription
        Subscription overdueSubscription = new Subscription();
        overdueSubscription.setCustomer(testCustomer);
        overdueSubscription.setPaymentPlan(testPlan);
        overdueSubscription.setBusinessId(testBusiness.getId());
        overdueSubscription.setStartDate(LocalDate.now().minusMonths(2));
        overdueSubscription.setNextPaymentDate(LocalDate.now().minusDays(5));
        overdueSubscription.setStatus(SubscriptionStatus.OVERDUE);
        overdueSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscriptionRepository.save(overdueSubscription);

        // Create active subscription (not overdue)
        Subscription activeSubscription = new Subscription();
        activeSubscription.setCustomer(testCustomer);
        activeSubscription.setPaymentPlan(testPlan);
        activeSubscription.setBusinessId(testBusiness.getId());
        activeSubscription.setStartDate(LocalDate.now());
        activeSubscription.setNextPaymentDate(LocalDate.now().plusDays(10));
        activeSubscription.setStatus(SubscriptionStatus.ACTIVE);
        activeSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscriptionRepository.save(activeSubscription);

        mockMvc.perform(get("/api/subscriptions/business/" + testBusiness.getId() + "/overdue")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[0].status").value("OVERDUE"));
    }

    @Test
    @DisplayName("Should get expiring soon subscriptions")
    void testGetExpiringSoonSubscriptions() throws Exception {
        // Create subscription expiring in 3 days
        Subscription expiringSubscription = new Subscription();
        expiringSubscription.setCustomer(testCustomer);
        expiringSubscription.setPaymentPlan(testPlan);
        expiringSubscription.setBusinessId(testBusiness.getId());
        expiringSubscription.setStartDate(LocalDate.now().minusDays(27));
        expiringSubscription.setNextPaymentDate(LocalDate.now().plusDays(3));
        expiringSubscription.setStatus(SubscriptionStatus.ACTIVE);
        expiringSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscriptionRepository.save(expiringSubscription);

        mockMvc.perform(get("/api/subscriptions/business/" + testBusiness.getId() + "/expiring-soon")
                        .header("Authorization", "Bearer " + authToken)
                        .param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should renew subscription successfully")
    void testRenewSubscription_Success() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setCustomer(testCustomer);
        subscription.setPaymentPlan(testPlan);
        subscription.setBusinessId(testBusiness.getId());
        subscription.setStartDate(LocalDate.now().minusMonths(1));
        subscription.setNextPaymentDate(LocalDate.now());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscription = subscriptionRepository.save(subscription);

        mockMvc.perform(post("/api/subscriptions/" + subscription.getId() + "/renew")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should cancel subscription successfully")
    void testCancelSubscription_Success() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setCustomer(testCustomer);
        subscription.setPaymentPlan(testPlan);
        subscription.setBusinessId(testBusiness.getId());
        subscription.setStartDate(LocalDate.now());
        subscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscription = subscriptionRepository.save(subscription);

        mockMvc.perform(post("/api/subscriptions/" + subscription.getId() + "/cancel")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should get customer subscriptions")
    void testGetCustomerSubscriptions() throws Exception {
        // Create multiple subscriptions for the customer
        for (int i = 0; i < 3; i++) {
            Subscription subscription = new Subscription();
            subscription.setCustomer(testCustomer);
            subscription.setPaymentPlan(testPlan);
            subscription.setBusinessId(testBusiness.getId());
            subscription.setStartDate(LocalDate.now().minusMonths(i));
            subscription.setNextPaymentDate(LocalDate.now().plusMonths(1 - i));
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setTotalAmount(BigDecimal.valueOf(500000));
            subscriptionRepository.save(subscription);
        }

        mockMvc.perform(get("/api/subscriptions/customer/" + testCustomer.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("Should update subscription notes")
    void testUpdateSubscription_Success() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setCustomer(testCustomer);
        subscription.setPaymentPlan(testPlan);
        subscription.setBusinessId(testBusiness.getId());
        subscription.setStartDate(LocalDate.now());
        subscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscription = subscriptionRepository.save(subscription);

        SubscriptionRequest updateRequest = new SubscriptionRequest();
        updateRequest.setCustomerId(testCustomer.getId());
        updateRequest.setPaymentPlanId(testPlan.getId());
        updateRequest.setStartDate(subscription.getStartDate());
        updateRequest.setNotes("یادداشت جدید");

        mockMvc.perform(put("/api/subscriptions/" + subscription.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should enforce multi-tenant isolation")
    void testGetSubscriptions_MultiTenantIsolation() throws Exception {
        // Create another business
        Business otherBusiness = new Business();
        otherBusiness.setName("Other Business");
        otherBusiness.setOwnerName("Other Owner");
        otherBusiness.setPhoneNumber("09129999999");
        otherBusiness.setAddress("Other Address");
        otherBusiness = businessRepository.save(otherBusiness);

        // Create customer for other business
        Customer otherCustomer = new Customer();
        otherCustomer.setFirstName("محمد");
        otherCustomer.setLastName("رضایی");
        otherCustomer.setPhoneNumber("09129876543");
        otherCustomer.setBusinessId(otherBusiness.getId());
        otherCustomer = customerRepository.save(otherCustomer);

        // Create subscription for other business
        Subscription otherSubscription = new Subscription();
        otherSubscription.setCustomer(otherCustomer);
        otherSubscription.setPaymentPlan(testPlan);
        otherSubscription.setBusinessId(otherBusiness.getId());
        otherSubscription.setStartDate(LocalDate.now());
        otherSubscription.setNextPaymentDate(LocalDate.now().plusMonths(1));
        otherSubscription.setStatus(SubscriptionStatus.ACTIVE);
        otherSubscription.setTotalAmount(BigDecimal.valueOf(500000));
        subscriptionRepository.save(otherSubscription);

        // Request with testBusiness token should not see otherBusiness subscription
        mockMvc.perform(get("/api/subscriptions/business/" + testBusiness.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }
}
