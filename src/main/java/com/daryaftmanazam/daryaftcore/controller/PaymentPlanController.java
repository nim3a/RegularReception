package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.ApiResponse;
import com.daryaftmanazam.daryaftcore.dto.request.PaymentPlanRequest;
import com.daryaftmanazam.daryaftcore.dto.response.PaymentPlanResponse;
import com.daryaftmanazam.daryaftcore.service.PaymentPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Payment Plan management operations.
 * Provides endpoints for managing payment plans within businesses.
 */
@RestController
@RequestMapping("/api/payment-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Plan Management", description = "APIs for managing payment plans")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentPlanController {

    private final PaymentPlanService paymentPlanService;

    /**
     * Create a new payment plan for a business.
     *
     * @param businessId Business ID
     * @param request    Payment plan creation request
     * @return Created payment plan with 201 status
     */
    @PostMapping("/business/{businessId}")
    @Operation(summary = "Create payment plan", description = "Creates a new payment plan for a specific business")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Payment plan created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentPlanResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentPlanResponse>> createPaymentPlan(
            @Parameter(description = "Business ID") @PathVariable Long businessId,
            @Valid @RequestBody PaymentPlanRequest request) {
        log.info("REST: Creating payment plan for business: {}", businessId);
        
        PaymentPlanResponse response = paymentPlanService.createPaymentPlan(businessId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<PaymentPlanResponse>builder()
                        .success(true)
                        .message("Payment plan created successfully")
                        .data(response)
                        .build());
    }

    /**
     * Get payment plan details by ID.
     *
     * @param id Payment plan ID
     * @return Payment plan details with 200 status
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get payment plan by ID", description = "Retrieves detailed information about a specific payment plan")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment plan found",
                    content = @Content(schema = @Schema(implementation = PaymentPlanResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment plan not found"
            )
    })
    public ResponseEntity<ApiResponse<PaymentPlanResponse>> getPaymentPlanById(
            @Parameter(description = "Payment plan ID") @PathVariable Long id) {
        log.info("REST: Fetching payment plan with id: {}", id);
        
        PaymentPlanResponse response = paymentPlanService.getPaymentPlanById(id);
        
        return ResponseEntity.ok(ApiResponse.<PaymentPlanResponse>builder()
                .success(true)
                .message("Payment plan retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Update payment plan information.
     *
     * @param id      Payment plan ID
     * @param request Payment plan update request
     * @return Updated payment plan with 200 status
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update payment plan", description = "Updates an existing payment plan's information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment plan updated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentPlanResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment plan not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public ResponseEntity<ApiResponse<PaymentPlanResponse>> updatePaymentPlan(
            @Parameter(description = "Payment plan ID") @PathVariable Long id,
            @Valid @RequestBody PaymentPlanRequest request) {
        log.info("REST: Updating payment plan with id: {}", id);
        
        PaymentPlanResponse response = paymentPlanService.updatePaymentPlan(id, request);
        
        return ResponseEntity.ok(ApiResponse.<PaymentPlanResponse>builder()
                .success(true)
                .message("Payment plan updated successfully")
                .data(response)
                .build());
    }

    /**
     * Delete (deactivate) a payment plan.
     *
     * @param id Payment plan ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment plan", description = "Deactivates a payment plan (sets isActive to false)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Payment plan deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Payment plan not found"
            )
    })
    public ResponseEntity<Void> deletePaymentPlan(
            @Parameter(description = "Payment plan ID") @PathVariable Long id) {
        log.info("REST: Deleting payment plan with id: {}", id);
        
        paymentPlanService.deletePaymentPlan(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * List payment plans for a business.
     *
     * @param businessId Business ID
     * @return List of payment plans with 200 status
     */
    @GetMapping("/business/{businessId}")
    @Operation(summary = "List payment plans by business", description = "Retrieves all active payment plans for a specific business")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment plans retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentPlanResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            )
    })
    public ResponseEntity<ApiResponse<List<PaymentPlanResponse>>> getPaymentPlansByBusiness(
            @Parameter(description = "Business ID") @PathVariable Long businessId) {
        log.info("REST: Fetching payment plans for business: {}", businessId);
        
        List<PaymentPlanResponse> plans = paymentPlanService.getPaymentPlansByBusiness(businessId);
        
        return ResponseEntity.ok(ApiResponse.<List<PaymentPlanResponse>>builder()
                .success(true)
                .message("Payment plans retrieved successfully")
                .data(plans)
                .build());
    }
}
