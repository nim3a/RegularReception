package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.request.CustomerRequest;
import com.daryaftmanazam.daryaftcore.dto.response.CustomerResponse;
import com.daryaftmanazam.daryaftcore.dto.response.SubscriptionResponse;
import com.daryaftmanazam.daryaftcore.model.enums.CustomerType;
import com.daryaftmanazam.daryaftcore.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CustomerController.
 */
@WebMvcTest(CustomerController.class)
@DisplayName("Customer Controller Integration Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    @DisplayName("Should add customer to business successfully")
    void shouldAddCustomerToBusinessSuccessfully() throws Exception {
        // Given
        Long businessId = 1L;
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("09123456789")
                .email("john@test.com")
                .customerType(CustomerType.INDIVIDUAL)
                .joinDate(LocalDate.now())
                .build();

        CustomerResponse response = CustomerResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("09123456789")
                .email("john@test.com")
                .customerType(CustomerType.INDIVIDUAL)
                .isActive(true)
                .businessId(businessId)
                .businessName("Test Business")
                .activeSubscriptionsCount(0)
                .build();

        when(customerService.addCustomer(eq(businessId), any(CustomerRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/customers/business/{businessId}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));

        verify(customerService, times(1)).addCustomer(eq(businessId), any(CustomerRequest.class));
    }

    @Test
    @DisplayName("Should get customer by ID successfully")
    void shouldGetCustomerByIdSuccessfully() throws Exception {
        // Given
        Long customerId = 1L;
        CustomerResponse response = CustomerResponse.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("09123456789")
                .isActive(true)
                .businessId(1L)
                .businessName("Test Business")
                .build();

        when(customerService.getCustomerById(customerId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(customerId))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(customerService, times(1)).getCustomerById(customerId);
    }

    @Test
    @DisplayName("Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() throws Exception {
        // Given
        Long customerId = 1L;
        CustomerRequest request = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("09123456789")
                .email("jane@test.com")
                .customerType(CustomerType.INDIVIDUAL)
                .build();

        CustomerResponse response = CustomerResponse.builder()
                .id(customerId)
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("09123456789")
                .email("jane@test.com")
                .isActive(true)
                .build();

        when(customerService.updateCustomer(eq(customerId), any(CustomerRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("Jane"));

        verify(customerService, times(1)).updateCustomer(eq(customerId), any(CustomerRequest.class));
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() throws Exception {
        // Given
        Long customerId = 1L;
        doNothing().when(customerService).deleteCustomer(customerId);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", customerId))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(customerId);
    }

    @Test
    @DisplayName("Should get customers by business with pagination")
    void shouldGetCustomersByBusinessWithPagination() throws Exception {
        // Given
        Long businessId = 1L;
        CustomerResponse customer1 = CustomerResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .businessId(businessId)
                .build();

        CustomerResponse customer2 = CustomerResponse.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .businessId(businessId)
                .build();

        Page<CustomerResponse> page = new PageImpl<>(
                List.of(customer1, customer2),
                PageRequest.of(0, 10),
                2
        );

        when(customerService.getCustomersByBusiness(eq(businessId), any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/customers/business/{businessId}", businessId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));

        verify(customerService, times(1)).getCustomersByBusiness(eq(businessId), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should search customers successfully")
    void shouldSearchCustomersSuccessfully() throws Exception {
        // Given
        Long businessId = 1L;
        String keyword = "John";
        List<CustomerResponse> customers = List.of(
                CustomerResponse.builder()
                        .id(1L)
                        .firstName("John")
                        .lastName("Doe")
                        .build()
        );

        when(customerService.searchCustomers(businessId, keyword)).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("businessId", String.valueOf(businessId))
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(customerService, times(1)).searchCustomers(businessId, keyword);
    }

    @Test
    @DisplayName("Should get customer subscriptions successfully")
    void shouldGetCustomerSubscriptionsSuccessfully() throws Exception {
        // Given
        Long customerId = 1L;
        List<SubscriptionResponse> subscriptions = List.of(
                SubscriptionResponse.builder()
                        .id(1L)
                        .customerId(customerId)
                        .customerName("John Doe")
                        .build()
        );

        when(customerService.getCustomerSubscriptionHistory(customerId)).thenReturn(subscriptions);

        // When & Then
        mockMvc.perform(get("/api/customers/{id}/subscriptions", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(customerService, times(1)).getCustomerSubscriptionHistory(customerId);
    }
}
