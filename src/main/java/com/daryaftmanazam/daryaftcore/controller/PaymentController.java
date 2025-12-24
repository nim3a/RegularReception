package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.ApiResponse;
import com.daryaftmanazam.daryaftcore.dto.PageResponse;
import com.daryaftmanazam.daryaftcore.dto.request.PaymentInitRequest;
import com.daryaftmanazam.daryaftcore.dto.request.PaymentRequest;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentDetailsDto;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentInitResponse;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentResponse;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentVerifyResponse;
import com.daryaftmanazam.daryaftcore.model.enums.PaymentStatus;
import com.daryaftmanazam.daryaftcore.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Payment management operations.
 * Provides endpoints for processing and managing payments.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "APIs for processing and managing payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Process a new payment.
     *
     * @param request Payment processing request
     * @return Processed payment with 201 status
     */
    @PostMapping
    @Operation(summary = "Process payment", description = "Processes a new payment for a subscription")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Payment processed successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or payment cannot be processed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("REST: Processing payment for subscription: {}", request.getSubscriptionId());
        
        PaymentResponse response = paymentService.processPayment(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<PaymentResponse>builder()
                        .success(true)
                        .message("Payment processed successfully")
                        .data(response)
                        .build());
    }

    /**
     * Get payment details by ID.
     *
     * @param id Payment ID
     * @return Payment details with 200 status
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves detailed information about a specific payment")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        log.info("REST: Fetching payment with id: {}", id);
        
        PaymentResponse response = paymentService.getPaymentById(id);
        
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get payments for a subscription.
     *
     * @param subscriptionId Subscription ID
     * @return List of payments with 200 status
     */
    @GetMapping("/subscription/{subscriptionId}")
    @Operation(summary = "Get payments by subscription", description = "Retrieves all payments for a specific subscription")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription not found"
            )
    })
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsBySubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long subscriptionId) {
        log.info("REST: Fetching payments for subscription: {}", subscriptionId);
        
        List<PaymentResponse> payments = paymentService.getPaymentsBySubscription(subscriptionId);
        
        return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
                .success(true)
                .message("Payments retrieved successfully")
                .data(payments)
                .build());
    }

    /**
     * Get payment history for a customer with pagination.
     *
     * @param customerId Customer ID
     * @param page       Page number (0-indexed)
     * @param size       Page size
     * @param sortBy     Sort field
     * @param sortDir    Sort direction (asc/desc)
     * @return Paginated payment history with 200 status
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer payment history", description = "Retrieves paginated payment history for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getCustomerPaymentHistory(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "paymentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("REST: Fetching payment history for customer: {} - page: {}, size: {}", customerId, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PaymentResponse> paymentPage = paymentService.getPaymentHistory(customerId, pageable);
        
        PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                .content(paymentPage.getContent())
                .pageNumber(paymentPage.getNumber())
                .pageSize(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .isLast(paymentPage.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.<PageResponse<PaymentResponse>>builder()
                .success(true)
                .message("Payment history retrieved successfully")
                .data(pageResponse)
                .build());
    }

    /**
     * Verify a payment by transaction ID.
     *
     * @param transactionId Transaction ID to verify
     * @return Verified payment with 200 status
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify payment", description = "Verifies a payment using its transaction ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment verified successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Parameter(description = "Transaction ID") @RequestParam String transactionId) {
        log.info("REST: Verifying payment with transaction ID: {}", transactionId);
        
        PaymentResponse response = paymentService.verifyPayment(transactionId);
        
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment verified successfully")
                .data(response)
                .build());
    }

    /**
     * Generate payment link for a subscription.
     *
     * @param subscriptionId Subscription ID
     * @return Payment link with 200 status
     */
    @GetMapping("/{subscriptionId}/link")
    @Operation(summary = "Generate payment link", description = "Generates a payment link for a subscription")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment link generated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Cannot generate link for cancelled subscription"
            )
    })
    public ResponseEntity<ApiResponse<String>> generatePaymentLink(
            @Parameter(description = "Subscription ID") @PathVariable Long subscriptionId) {
        log.info("REST: Generating payment link for subscription: {}", subscriptionId);
        
        String paymentLink = paymentService.generatePaymentLink(subscriptionId);
        
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Payment link generated successfully")
                .data(paymentLink)
                .build());
    }

    /**
     * Get pending payments for a customer.
     *
     * @param customerId Customer ID
     * @return List of pending payments with 200 status
     */
    @GetMapping("/customer/{customerId}/pending")
    @Operation(summary = "Get pending payments", description = "Retrieves all pending payments for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPendingPayments(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        log.info("REST: Fetching pending payments for customer: {}", customerId);
        
        List<PaymentResponse> pendingPayments = paymentService.getPendingPayments(customerId);
        
        return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
                .success(true)
                .message("Pending payments retrieved successfully")
                .data(pendingPayments)
                .build());
    }

    // ==================== MOCK PAYMENT GATEWAY ENDPOINTS ====================

    /**
     * Initiate a payment through the mock gateway.
     * Creates a PENDING payment record and returns a mock payment URL.
     *
     * @param request Payment initiation request
     * @return Payment initiation response with payment URL
     */
    @PostMapping("/initiate")
    @Operation(summary = "Initiate mock payment", description = "Initiates a payment and returns a mock payment gateway URL for testing")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentInitResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription not found"
            )
    })
    public ResponseEntity<PaymentInitResponse> initiatePayment(
            @Valid @RequestBody PaymentInitRequest request) {
        log.info("REST: Initiating payment for subscription: {}", request.getSubscriptionId());
        
        PaymentInitResponse response = paymentService.initiatePayment(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify and complete a payment from the mock gateway.
     * This endpoint is called after the user completes payment on the mock gateway page.
     *
     * @param transactionId Transaction ID to verify
     * @param success Whether payment was successful
     * @return Payment verification response
     */
    @PostMapping("/verify-mock")
    @Operation(summary = "Verify mock payment", description = "Verifies and completes a payment initiated through the mock gateway")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment verified",
                    content = @Content(schema = @Schema(implementation = PaymentVerifyResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    public ResponseEntity<PaymentVerifyResponse> verifyMockPayment(
            @Parameter(description = "Transaction ID") @RequestParam String transactionId,
            @Parameter(description = "Payment success status") @RequestParam boolean success) {
        log.info("REST: Verifying mock payment - Transaction: {}, Success: {}", transactionId, success);
        
        PaymentVerifyResponse response = paymentService.verifyMockPayment(transactionId, success);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment details for the mock payment gateway page.
     * Returns customer, plan, and payment information for display on the gateway.
     *
     * @param transactionId Transaction ID
     * @return Payment details DTO
     */
    @GetMapping("/details")
    @Operation(summary = "Get payment details for gateway", description = "Retrieves payment details to display on the mock payment gateway page")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDetailsDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    public ResponseEntity<PaymentDetailsDto> getPaymentDetails(
            @Parameter(description = "Transaction ID") @RequestParam String transactionId) {
        log.info("REST: Fetching payment details for transaction: {}", transactionId);
        
        PaymentDetailsDto details = paymentService.getPaymentDetails(transactionId);
        
        return ResponseEntity.ok(details);
    }

    /**
     * Get payment history for a business with optional filters.
     * Useful for business dashboards and reporting.
     *
     * @param businessId Business ID
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param status Optional payment status filter
     * @return List of payments matching the filters
     */
    @GetMapping("/history")
    @Operation(summary = "Get payment history", description = "Retrieves payment history for a business with optional filters")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment history retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentHistory(
            @Parameter(description = "Business ID") @RequestParam Long businessId,
            @Parameter(description = "Start date (optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (optional)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Payment status filter (optional)") 
            @RequestParam(required = false) PaymentStatus status) {
        log.info("REST: Fetching payment history for business: {} with filters", businessId);
        
        List<PaymentResponse> payments = paymentService.getPaymentHistory(businessId, startDate, endDate, status);
        
        return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
                .success(true)
                .message("Payment history retrieved successfully")
                .data(payments)
                .build());
    }

    // ==================== END MOCK PAYMENT GATEWAY ENDPOINTS ====================
}
