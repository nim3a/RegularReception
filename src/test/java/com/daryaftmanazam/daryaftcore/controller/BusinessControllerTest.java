package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.request.BusinessRequest;
import com.daryaftmanazam.daryaftcore.dto.response.BusinessDashboardResponse;
import com.daryaftmanazam.daryaftcore.dto.response.BusinessResponse;
import com.daryaftmanazam.daryaftcore.service.BusinessService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BusinessController.
 */
@WebMvcTest(BusinessController.class)
@DisplayName("Business Controller Integration Tests")
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusinessService businessService;

    @Test
    @DisplayName("Should create business successfully")
    void shouldCreateBusinessSuccessfully() throws Exception {
        // Given
        BusinessRequest request = BusinessRequest.builder()
                .businessName("Test Business")
                .ownerName("John Doe")
                .contactEmail("john@test.com")
                .contactPhone("09123456789")
                .description("Test Description")
                .build();

        BusinessResponse response = BusinessResponse.builder()
                .id(1L)
                .businessName("Test Business")
                .ownerName("John Doe")
                .contactEmail("john@test.com")
                .contactPhone("09123456789")
                .description("Test Description")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .customerCount(0L)
                .build();

        when(businessService.createBusiness(any(BusinessRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Business created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.businessName").value("Test Business"))
                .andExpect(jsonPath("$.data.ownerName").value("John Doe"));

        verify(businessService, times(1)).createBusiness(any(BusinessRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when business name is missing")
    void shouldReturn400WhenBusinessNameIsMissing() throws Exception {
        // Given
        BusinessRequest request = BusinessRequest.builder()
                .ownerName("John Doe")
                .contactEmail("john@test.com")
                .build();

        // When & Then
        mockMvc.perform(post("/api/businesses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(businessService, never()).createBusiness(any(BusinessRequest.class));
    }

    @Test
    @DisplayName("Should get business by ID successfully")
    void shouldGetBusinessByIdSuccessfully() throws Exception {
        // Given
        Long businessId = 1L;
        BusinessResponse response = BusinessResponse.builder()
                .id(businessId)
                .businessName("Test Business")
                .ownerName("John Doe")
                .isActive(true)
                .customerCount(5L)
                .build();

        when(businessService.getBusinessById(businessId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/businesses/{id}", businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(businessId))
                .andExpect(jsonPath("$.data.businessName").value("Test Business"));

        verify(businessService, times(1)).getBusinessById(businessId);
    }

    @Test
    @DisplayName("Should update business successfully")
    void shouldUpdateBusinessSuccessfully() throws Exception {
        // Given
        Long businessId = 1L;
        BusinessRequest request = BusinessRequest.builder()
                .businessName("Updated Business")
                .ownerName("Jane Doe")
                .contactEmail("jane@test.com")
                .contactPhone("09123456789")
                .build();

        BusinessResponse response = BusinessResponse.builder()
                .id(businessId)
                .businessName("Updated Business")
                .ownerName("Jane Doe")
                .contactEmail("jane@test.com")
                .isActive(true)
                .build();

        when(businessService.updateBusiness(eq(businessId), any(BusinessRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/businesses/{id}", businessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.businessName").value("Updated Business"));

        verify(businessService, times(1)).updateBusiness(eq(businessId), any(BusinessRequest.class));
    }

    @Test
    @DisplayName("Should delete business successfully")
    void shouldDeleteBusinessSuccessfully() throws Exception {
        // Given
        Long businessId = 1L;
        doNothing().when(businessService).deleteBusiness(businessId);

        // When & Then
        mockMvc.perform(delete("/api/businesses/{id}", businessId))
                .andExpect(status().isNoContent());

        verify(businessService, times(1)).deleteBusiness(businessId);
    }

    @Test
    @DisplayName("Should get all businesses with pagination")
    void shouldGetAllBusinessesWithPagination() throws Exception {
        // Given
        BusinessResponse business1 = BusinessResponse.builder()
                .id(1L)
                .businessName("Business 1")
                .ownerName("Owner 1")
                .isActive(true)
                .build();

        BusinessResponse business2 = BusinessResponse.builder()
                .id(2L)
                .businessName("Business 2")
                .ownerName("Owner 2")
                .isActive(true)
                .build();

        Page<BusinessResponse> page = new PageImpl<>(
                List.of(business1, business2),
                PageRequest.of(0, 10),
                2
        );

        when(businessService.getAllBusinesses(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/businesses")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(businessService, times(1)).getAllBusinesses(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should get business dashboard successfully")
    void shouldGetBusinessDashboardSuccessfully() throws Exception {
        // Given
        Long businessId = 1L;
        BusinessDashboardResponse dashboard = BusinessDashboardResponse.builder()
                .businessId(businessId)
                .businessName("Test Business")
                .totalCustomers(10)
                .activeCustomers(8)
                .activeSubscriptions(15)
                .overdueSubscriptions(2)
                .totalRevenue(BigDecimal.valueOf(10000))
                .pendingPayments(BigDecimal.valueOf(1500))
                .overdueAmount(BigDecimal.valueOf(500))
                .build();

        when(businessService.getBusinessDashboard(businessId)).thenReturn(dashboard);

        // When & Then
        mockMvc.perform(get("/api/businesses/{id}/dashboard", businessId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.businessId").value(businessId))
                .andExpect(jsonPath("$.data.totalCustomers").value(10))
                .andExpect(jsonPath("$.data.activeSubscriptions").value(15));

        verify(businessService, times(1)).getBusinessDashboard(businessId);
    }
}
