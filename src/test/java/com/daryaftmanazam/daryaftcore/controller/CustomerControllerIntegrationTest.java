package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.BaseIntegrationTest;
import com.daryaftmanazam.daryaftcore.dto.request.CustomerRequest;
import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.Customer;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import com.daryaftmanazam.daryaftcore.repository.BusinessRepository;
import com.daryaftmanazam.daryaftcore.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CustomerController
 * Tests REST API endpoints, authentication, and multi-tenant isolation
 */
@DisplayName("Customer Controller Integration Tests")
class CustomerControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BusinessRepository businessRepository;

    private Business testBusiness1;
    private Business testBusiness2;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Clean up
        customerRepository.deleteAll();
        businessRepository.deleteAll();

        // Create test businesses
        testBusiness1 = new Business();
        testBusiness1.setName("Test Business 1");
        testBusiness1.setOwnerName("Owner 1");
        testBusiness1.setPhoneNumber("09121111111");
        testBusiness1.setAddress("Address 1");
        testBusiness1 = businessRepository.save(testBusiness1);

        testBusiness2 = new Business();
        testBusiness2.setName("Test Business 2");
        testBusiness2.setOwnerName("Owner 2");
        testBusiness2.setPhoneNumber("09122222222");
        testBusiness2.setAddress("Address 2");
        testBusiness2 = businessRepository.save(testBusiness2);

        // Generate test token for business 1
        authToken = generateTestToken("testuser", testBusiness1.getId(), "BUSINESS_ADMIN");
    }

    @Test
    @DisplayName("Should create customer successfully with valid data")
    void testCreateCustomer_Success() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("علی");
        request.setLastName("احمدی");
        request.setPhoneNumber("09121234567");
        request.setNationalId("1234567890");
        request.setEmail("ali@test.com");
        request.setCustomerType(CustomerType.REGULAR);

        mockMvc.perform(post("/api/customers/business/" + testBusiness1.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("علی"))
                .andExpect(jsonPath("$.data.lastName").value("احمدی"))
                .andExpect(jsonPath("$.data.phoneNumber").value("09121234567"))
                .andExpect(jsonPath("$.data.customerType").value("REGULAR"));
    }

    @Test
    @DisplayName("Should return 401 when creating customer without authentication")
    void testCreateCustomer_WithoutAuth_Unauthorized() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("علی");
        request.setLastName("احمدی");
        request.setPhoneNumber("09121234567");

        mockMvc.perform(post("/api/customers/business/" + testBusiness1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return validation errors for invalid customer data")
    void testCreateCustomer_InvalidData_ValidationError() throws Exception {
        CustomerRequest request = new CustomerRequest();
        // Missing required fields

        mockMvc.perform(post("/api/customers/business/" + testBusiness1.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should enforce multi-tenant isolation when listing customers")
    void testGetCustomers_MultiTenantIsolation() throws Exception {
        // Create customers for business 1
        Customer customer1 = new Customer();
        customer1.setFirstName("Customer1");
        customer1.setLastName("Business1");
        customer1.setPhoneNumber("09121111111");
        customer1.setBusinessId(testBusiness1.getId());
        customerRepository.save(customer1);

        // Create customers for business 2
        Customer customer2 = new Customer();
        customer2.setFirstName("Customer2");
        customer2.setLastName("Business2");
        customer2.setPhoneNumber("09122222222");
        customer2.setBusinessId(testBusiness2.getId());
        customerRepository.save(customer2);

        // Request with business 1 token should only see business 1 customers
        mockMvc.perform(get("/api/customers/business/" + testBusiness1.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].firstName").value("Customer1"));
    }

    @Test
    @DisplayName("Should get customer by ID successfully")
    void testGetCustomerById_Success() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("محمد");
        customer.setLastName("رضایی");
        customer.setPhoneNumber("09123456789");
        customer.setBusinessId(testBusiness1.getId());
        customer = customerRepository.save(customer);

        mockMvc.perform(get("/api/customers/" + customer.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("محمد"))
                .andExpect(jsonPath("$.data.lastName").value("رضایی"));
    }

    @Test
    @DisplayName("Should return 404 when customer not found")
    void testGetCustomerById_NotFound() throws Exception {
        mockMvc.perform(get("/api/customers/99999")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update customer successfully")
    void testUpdateCustomer_Success() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("حسن");
        customer.setLastName("محمدی");
        customer.setPhoneNumber("09111111111");
        customer.setBusinessId(testBusiness1.getId());
        customer = customerRepository.save(customer);

        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setFirstName("حسین");
        updateRequest.setLastName("محمدی");
        updateRequest.setPhoneNumber("09111111111");
        updateRequest.setCustomerType(CustomerType.VIP);

        mockMvc.perform(put("/api/customers/" + customer.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("حسین"))
                .andExpect(jsonPath("$.data.customerType").value("VIP"));
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void testDeleteCustomer_Success() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("احمد");
        customer.setLastName("کریمی");
        customer.setPhoneNumber("09199999999");
        customer.setBusinessId(testBusiness1.getId());
        customer = customerRepository.save(customer);

        mockMvc.perform(delete("/api/customers/" + customer.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should search customers by phone number")
    void testSearchCustomers_ByPhoneNumber() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("سارا");
        customer.setLastName("احمدی");
        customer.setPhoneNumber("09121234567");
        customer.setBusinessId(testBusiness1.getId());
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customers/business/" + testBusiness1.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .param("phoneNumber", "09121234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.content[?(@.phoneNumber=='09121234567')]").exists());
    }

    @Test
    @DisplayName("Should support pagination for customer list")
    void testGetCustomers_WithPagination() throws Exception {
        // Create multiple customers
        for (int i = 0; i < 15; i++) {
            Customer customer = new Customer();
            customer.setFirstName("Customer" + i);
            customer.setLastName("Test");
            customer.setPhoneNumber("0912000000" + i);
            customer.setBusinessId(testBusiness1.getId());
            customerRepository.save(customer);
        }

        mockMvc.perform(get("/api/customers/business/" + testBusiness1.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(10)))
                .andExpect(jsonPath("$.data.totalElements").value(15))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }
}
