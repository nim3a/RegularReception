package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.ApiResponse;
import com.daryaftmanazam.daryaftcore.dto.request.SubscriptionRequest;
import com.daryaftmanazam.daryaftcore.dto.response.SubscriptionResponse;
import com.daryaftmanazam.daryaftcore.model.enums.SubscriptionStatus;
import com.daryaftmanazam.daryaftcore.service.SubscriptionService;
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
 * REST Controller for Subscription management operations.
 * Provides endpoints for managing customer subscriptions.
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "APIs for managing customer subscriptions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Create a new subscription.
     *
     * @param request Subscription creation request
     * @return Created subscription with 201 status
     */
    @PostMapping
    @Operation(summary = "Create subscription", description = "Creates a new subscription for a customer with a payment plan")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Subscription created successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer or Payment Plan not found"
            )
    })
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody SubscriptionRequest request) {
        log.info("REST: Creating subscription for customer: {}", request.getCustomerId());
        
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<SubscriptionResponse>builder()
                        .success(true)
                        .message("Subscription created successfully")
                        .data(response)
                        .build());
    }

    /**
     * Get subscription details by ID.
     *
     * @param id Subscription ID
     * @return Subscription details with 200 status
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by ID", description = "Retrieves detailed information about a specific subscription")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription found",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription not found"
            )
    })
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        log.info("REST: Fetching subscription with id: {}", id);
        
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        
        return ResponseEntity.ok(ApiResponse.<SubscriptionResponse>builder()
                .success(true)
                .message("Subscription retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Renew an existing subscription.
     *
     * @param id Subscription ID
     * @return Renewed subscription with 200 status
     */
    @PutMapping("/{id}/renew")
    @Operation(summary = "Renew subscription", description = "Renews an existing subscription for another period")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription renewed successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Subscription cannot be renewed (e.g., cancelled)"
            )
    })
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        log.info("REST: Renewing subscription: {}", id);
        
        SubscriptionResponse response = subscriptionService.renewSubscription(id);
        
        return ResponseEntity.ok(ApiResponse.<SubscriptionResponse>builder()
                .success(true)
                .message("Subscription renewed successfully")
                .data(response)
                .build());
    }

    /**
     * Cancel a subscription.
     *
     * @param id Subscription ID
     * @return Cancelled subscription with 200 status
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancels an active subscription")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription cancelled successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Subscription not found"
            )
    })
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        log.info("REST: Cancelling subscription: {}", id);
        
        SubscriptionResponse response = subscriptionService.cancelSubscription(id);
        
        return ResponseEntity.ok(ApiResponse.<SubscriptionResponse>builder()
                .success(true)
                .message("Subscription cancelled successfully")
                .data(response)
                .build());
    }

    /**
     * Get subscriptions for a customer.
     *
     * @param customerId Customer ID
     * @return List of customer subscriptions with 200 status
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer subscriptions", description = "Retrieves all active subscriptions for a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscriptions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getCustomerSubscriptions(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        log.info("REST: Fetching subscriptions for customer: {}", customerId);
        
        List<SubscriptionResponse> subscriptions = subscriptionService.getActiveSubscriptions(customerId);
        
        return ResponseEntity.ok(ApiResponse.<List<SubscriptionResponse>>builder()
                .success(true)
                .message("Subscriptions retrieved successfully")
                .data(subscriptions)
                .build());
    }

    /**
     * Get subscriptions by status.
     *
     * @param status Subscription status
     * @return List of subscriptions with the specified status with 200 status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get subscriptions by status", description = "Retrieves all subscriptions with a specific status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscriptions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid status"
            )
    })
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionsByStatus(
            @Parameter(description = "Subscription status (ACTIVE, OVERDUE, EXPIRED, CANCELLED)") 
            @PathVariable SubscriptionStatus status) {
        log.info("REST: Fetching subscriptions with status: {}", status);
        
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByStatus(status);
        
        return ResponseEntity.ok(ApiResponse.<List<SubscriptionResponse>>builder()
                .success(true)
                .message("Subscriptions retrieved successfully")
                .data(subscriptions)
                .build());
    }
}
