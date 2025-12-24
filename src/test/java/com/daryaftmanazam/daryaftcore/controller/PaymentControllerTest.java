package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.request.PaymentRequest;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentResponse;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentMethod;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import com.daryaftmanazam.daryaftcore.service.PaymentService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for PaymentController.
 */
@WebMvcTest(PaymentController.class)
@DisplayName("Payment Controller Integration Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        PaymentRequest request = PaymentRequest.builder()
                .subscriptionId(1L)
                .amount(BigDecimal.valueOf(1000))
                .paymentMethod(PaymentMethod.CASH)
                .notes("Test payment")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .subscriptionId(1L)
                .customerName("John Doe")
                .amount(BigDecimal.valueOf(1000))
                .paymentDate(LocalDateTime.now())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CASH)
                .transactionId("TXN-12345")
                .build();

        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        verify(paymentService, times(1)).processPayment(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("Should get payment by ID successfully")
    void shouldGetPaymentByIdSuccessfully() throws Exception {
        // Given
        Long paymentId = 1L;
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .subscriptionId(1L)
                .customerName("John Doe")
                .amount(BigDecimal.valueOf(1000))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentService.getPaymentById(paymentId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(paymentId));

        verify(paymentService, times(1)).getPaymentById(paymentId);
    }

    @Test
    @DisplayName("Should get payments by subscription successfully")
    void shouldGetPaymentsBySubscriptionSuccessfully() throws Exception {
        // Given
        Long subscriptionId = 1L;
        List<PaymentResponse> payments = List.of(
                PaymentResponse.builder()
                        .id(1L)
                        .subscriptionId(subscriptionId)
                        .amount(BigDecimal.valueOf(1000))
                        .build()
        );

        when(paymentService.getPaymentsBySubscription(subscriptionId)).thenReturn(payments);

        // When & Then
        mockMvc.perform(get("/api/payments/subscription/{subscriptionId}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(paymentService, times(1)).getPaymentsBySubscription(subscriptionId);
    }

    @Test
    @DisplayName("Should get customer payment history with pagination")
    void shouldGetCustomerPaymentHistoryWithPagination() throws Exception {
        // Given
        Long customerId = 1L;
        PaymentResponse payment1 = PaymentResponse.builder()
                .id(1L)
                .customerName("John Doe")
                .amount(BigDecimal.valueOf(1000))
                .build();

        Page<PaymentResponse> page = new PageImpl<>(
                List.of(payment1),
                PageRequest.of(0, 10),
                1
        );

        when(paymentService.getPaymentHistory(eq(customerId), any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/payments/customer/{customerId}", customerId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(paymentService, times(1)).getPaymentHistory(eq(customerId), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should verify payment successfully")
    void shouldVerifyPaymentSuccessfully() throws Exception {
        // Given
        String transactionId = "TXN-12345";
        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .transactionId(transactionId)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentService.verifyPayment(transactionId)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/payments/verify")
                        .param("transactionId", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value(transactionId));

        verify(paymentService, times(1)).verifyPayment(transactionId);
    }

    @Test
    @DisplayName("Should generate payment link successfully")
    void shouldGeneratePaymentLinkSuccessfully() throws Exception {
        // Given
        Long subscriptionId = 1L;
        String paymentLink = "https://payment.example.com/pay/1/abc123";

        when(paymentService.generatePaymentLink(subscriptionId)).thenReturn(paymentLink);

        // When & Then
        mockMvc.perform(get("/api/payments/{subscriptionId}/link", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(paymentLink));

        verify(paymentService, times(1)).generatePaymentLink(subscriptionId);
    }

    @Test
    @DisplayName("Should get pending payments successfully")
    void shouldGetPendingPaymentsSuccessfully() throws Exception {
        // Given
        Long customerId = 1L;
        List<PaymentResponse> pendingPayments = List.of(
                PaymentResponse.builder()
                        .subscriptionId(1L)
                        .customerName("John Doe")
                        .amount(BigDecimal.valueOf(1000))
                        .status(PaymentStatus.PENDING)
                        .dueDate(LocalDate.now())
                        .build()
        );

        when(paymentService.getPendingPayments(customerId)).thenReturn(pendingPayments);

        // When & Then
        mockMvc.perform(get("/api/payments/customer/{customerId}/pending", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(paymentService, times(1)).getPendingPayments(customerId);
    }
}
