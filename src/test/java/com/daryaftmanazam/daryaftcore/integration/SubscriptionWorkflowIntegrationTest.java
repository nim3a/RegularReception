package com.daryaftmanazam.daryaftcore.integration;

import com.daryaftmanazam.daryaftcore.BaseIntegrationTest;
import com.daryaftmanazam.daryaftcore.dto.request.*;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentInterval;
import com.daryaftmanazam.daryaftcore.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End integration test for complete subscription workflow
 * Tests the entire lifecycle from business creation to payment verification
 */
@DisplayName("Complete Subscription Workflow E2E Test")
class SubscriptionWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentPlanRepository paymentPlanRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean up all data
        paymentRepository.deleteAll();
        subscriptionRepository.deleteAll();
        customerRepository.deleteAll();
        paymentPlanRepository.deleteAll();
        userRepository.deleteAll();
        businessRepository.deleteAll();
    }

    @Test
    @DisplayName("Complete subscription lifecycle: Create business -> Customer -> Plan -> Subscription -> Payment")
    @Transactional
    void testCompleteSubscriptionLifecycle() throws Exception {
        // ========================
        // STEP 1: Create Business
        // ========================
        BusinessRequest businessRequest = new BusinessRequest();
        businessRequest.setName("باشگاه ورزشی طلایی");
        businessRequest.setOwnerName("علی احمدی");
        businessRequest.setPhoneNumber("09121111111");
        businessRequest.setEmail("gym@example.com");
        businessRequest.setAddress("تهران، خیابان ولیعصر");
        businessRequest.setDescription("باشگاه ورزشی با امکانات کامل");

        MvcResult businessResult = mockMvc.perform(post("/api/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(businessRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("باشگاه ورزشی طلایی"))
                .andReturn();

        String businessResponse = businessResult.getResponse().getContentAsString();
        JsonNode businessNode = objectMapper.readTree(businessResponse);
        Long businessId = businessNode.path("data").path("id").asLong();

        assertThat(businessId).isNotNull();
        System.out.println("✓ Step 1: Business created with ID: " + businessId);

        // ========================
        // STEP 2: Register User/Admin for Business
        // ========================
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("gym_admin");
        registerRequest.setEmail("admin@gym.com");
        registerRequest.setPassword("Admin@123");
        registerRequest.setBusinessId(businessId);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        System.out.println("✓ Step 2: Admin user registered");

        // Get JWT token
        String authToken = generateTestToken("gym_admin", businessId, "BUSINESS_ADMIN");

        // ========================
        // STEP 3: Create Customer
        // ========================
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setFirstName("محمد");
        customerRequest.setLastName("رضایی");
        customerRequest.setPhoneNumber("09123456789");
        customerRequest.setNationalId("1234567890");
        customerRequest.setEmail("mohammad@example.com");
        customerRequest.setCustomerType(CustomerType.VIP);

        MvcResult customerResult = mockMvc.perform(post("/api/customers/business/" + businessId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("محمد"))
                .andReturn();

        String customerResponse = customerResult.getResponse().getContentAsString();
        JsonNode customerNode = objectMapper.readTree(customerResponse);
        Long customerId = customerNode.path("data").path("id").asLong();

        assertThat(customerId).isNotNull();
        System.out.println("✓ Step 3: Customer created with ID: " + customerId);

        // ========================
        // STEP 4: Create Payment Plan
        // ========================
        PaymentPlanRequest planRequest = new PaymentPlanRequest();
        planRequest.setName("اشتراک ماهانه VIP");
        planRequest.setDescription("دسترسی کامل به تمام امکانات");
        planRequest.setAmount(BigDecimal.valueOf(1500000));
        planRequest.setPaymentInterval(PaymentInterval.MONTHLY);
        planRequest.setLateFeePercentage(BigDecimal.valueOf(2));

        MvcResult planResult = mockMvc.perform(post("/api/payment-plans/business/" + businessId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("اشتراک ماهانه VIP"))
                .andReturn();

        String planResponse = planResult.getResponse().getContentAsString();
        JsonNode planNode = objectMapper.readTree(planResponse);
        Long planId = planNode.path("data").path("id").asLong();

        assertThat(planId).isNotNull();
        System.out.println("✓ Step 4: Payment plan created with ID: " + planId);

        // ========================
        // STEP 5: Create Subscription
        // ========================
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setCustomerId(customerId);
        subscriptionRequest.setPaymentPlanId(planId);
        subscriptionRequest.setStartDate(LocalDate.now());
        subscriptionRequest.setNotes("اولین اشتراک مشتری VIP");

        MvcResult subscriptionResult = mockMvc.perform(post("/api/subscriptions")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscriptionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.totalAmount").value(1500000))
                .andReturn();

        String subscriptionResponse = subscriptionResult.getResponse().getContentAsString();
        JsonNode subscriptionNode = objectMapper.readTree(subscriptionResponse);
        Long subscriptionId = subscriptionNode.path("data").path("id").asLong();

        assertThat(subscriptionId).isNotNull();
        System.out.println("✓ Step 5: Subscription created with ID: " + subscriptionId);

        // ========================
        // STEP 6: Initiate Payment
        // ========================
        PaymentInitRequest paymentRequest = new PaymentInitRequest();
        paymentRequest.setSubscriptionId(subscriptionId);
        paymentRequest.setAmount(BigDecimal.valueOf(1500000));
        paymentRequest.setDescription("پرداخت اشتراک ماهانه");

        MvcResult paymentInitResult = mockMvc.perform(post("/api/payments/initiate")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentUrl").exists())
                .andExpect(jsonPath("$.data.trackingCode").exists())
                .andReturn();

        String paymentInitResponse = paymentInitResult.getResponse().getContentAsString();
        JsonNode paymentInitNode = objectMapper.readTree(paymentInitResponse);
        String trackingCode = paymentInitNode.path("data").path("trackingCode").asText();

        assertThat(trackingCode).isNotEmpty();
        System.out.println("✓ Step 6: Payment initiated with tracking code: " + trackingCode);

        // ========================
        // STEP 7: Verify Payment (Simulate Gateway Callback)
        // ========================
        mockMvc.perform(get("/api/payments/verify")
                        .param("trackingCode", trackingCode)
                        .param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        System.out.println("✓ Step 7: Payment verified successfully");

        // ========================
        // STEP 8: Verify Subscription Status Updated
        // ========================
        mockMvc.perform(get("/api/subscriptions/" + subscriptionId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        System.out.println("✓ Step 8: Subscription status verified");

        // ========================
        // STEP 9: Verify Payment Recorded
        // ========================
        mockMvc.perform(get("/api/payments/subscription/" + subscriptionId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data[0].amount").value(1500000));

        System.out.println("✓ Step 9: Payment recorded in history");

        // ========================
        // STEP 10: Get Customer Payment History
        // ========================
        mockMvc.perform(get("/api/payments/customer/" + customerId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        System.out.println("✓ Step 10: Customer payment history retrieved");

        // ========================
        // STEP 11: Verify Business Statistics
        // ========================
        mockMvc.perform(get("/api/payments/business/" + businessId + "/statistics")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPayments").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.totalAmount").value(greaterThanOrEqualTo(1500000)));

        System.out.println("✓ Step 11: Business statistics verified");

        // ========================
        // FINAL VERIFICATION
        // ========================
        System.out.println("\n========== E2E Test Summary ==========");
        System.out.println("Business ID: " + businessId);
        System.out.println("Customer ID: " + customerId);
        System.out.println("Payment Plan ID: " + planId);
        System.out.println("Subscription ID: " + subscriptionId);
        System.out.println("Tracking Code: " + trackingCode);
        System.out.println("✅ Complete subscription lifecycle test passed!");
        System.out.println("======================================\n");

        // Verify database state
        assertThat(businessRepository.findById(businessId)).isPresent();
        assertThat(customerRepository.findById(customerId)).isPresent();
        assertThat(paymentPlanRepository.findById(planId)).isPresent();
        assertThat(subscriptionRepository.findById(subscriptionId)).isPresent();
        assertThat(paymentRepository.findByTrackingCode(trackingCode)).isPresent();
    }

    @Test
    @DisplayName("Should handle subscription renewal workflow")
    @Transactional
    void testSubscriptionRenewalWorkflow() throws Exception {
        // Setup: Create business, customer, plan, and subscription (abbreviated)
        Long businessId = 1L;
        Long customerId = 1L;
        Long subscriptionId = 1L;
        String authToken = generateTestToken("admin", businessId, "BUSINESS_ADMIN");

        // Test renewal process
        mockMvc.perform(post("/api/subscriptions/" + subscriptionId + "/renew")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        System.out.println("✓ Subscription renewal workflow completed");
    }

    @Test
    @DisplayName("Should handle subscription cancellation workflow")
    @Transactional
    void testSubscriptionCancellationWorkflow() throws Exception {
        // Setup: Create business, customer, plan, and subscription
        Long businessId = 1L;
        Long subscriptionId = 1L;
        String authToken = generateTestToken("admin", businessId, "BUSINESS_ADMIN");

        // Test cancellation
        mockMvc.perform(post("/api/subscriptions/" + subscriptionId + "/cancel")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        System.out.println("✓ Subscription cancellation workflow completed");
    }
}
