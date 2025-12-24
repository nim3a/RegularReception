package com.daryaftmanazam.daryaftcore.exception;

import com.daryaftmanazam.daryaftcore.dto.request.BusinessRequestDto;
import com.daryaftmanazam.daryaftcore.dto.request.CustomerRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for exception handling through REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Exception Handling Integration Tests")
class ExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET non-existent business should return 404 with Persian error")
    void testBusinessNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/businesses/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error_code").value("BUSINESS_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/businesses/99999"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("GET non-existent customer should return 404 with Persian error")
    void testCustomerNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/customers/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error_code").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/customers/99999"));
    }

    @Test
    @DisplayName("GET non-existent subscription should return 404 with Persian error")
    void testSubscriptionNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/subscriptions/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error_code").value("SUBSCRIPTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST business with invalid data should return 400 with field errors")
    void testValidationErrorReturns400() throws Exception {
        BusinessRequestDto invalidBusiness = new BusinessRequestDto();
        // Empty required fields will trigger validation errors
        
        mockMvc.perform(post("/api/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBusiness))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error_code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.field_errors").isMap());
    }

    @Test
    @DisplayName("POST customer with invalid data should return 400 with field errors")
    void testCustomerValidationErrorReturns400() throws Exception {
        CustomerRequestDto invalidCustomer = new CustomerRequestDto();
        // Empty required fields will trigger validation errors
        
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error_code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.field_errors").exists());
    }

    @Test
    @DisplayName("POST with malformed JSON should return 400")
    void testMalformedJsonReturns400() throws Exception {
        String malformedJson = "{ invalid json }";
        
        mockMvc.perform(post("/api/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET with invalid path parameter type should return 400")
    void testInvalidPathParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/businesses/invalid-id")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("All error responses should have consistent structure")
    void testErrorResponseStructure() throws Exception {
        mockMvc.perform(get("/api/businesses/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").isNumber())
                .andExpect(jsonPath("$.error_code").isString())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.path").isString());
    }

    @Test
    @DisplayName("GET non-existent payment plan should return 404")
    void testPaymentPlanNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/payment-plans/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error_code").value("PAYMENT_PLAN_NOT_FOUND"));
    }

    @Test
    @DisplayName("All 404 errors should include Persian message")
    void testPersianMessages() throws Exception {
        // Test Business not found
        mockMvc.perform(get("/api/businesses/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("کسب")));

        // Test Customer not found
        mockMvc.perform(get("/api/customers/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("مشتری")));

        // Test Subscription not found
        mockMvc.perform(get("/api/subscriptions/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("اشتراک")));
    }
}
