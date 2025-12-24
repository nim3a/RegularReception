package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.ApiResponse;
import com.daryaftmanazam.daryaftcore.dto.PageResponse;
import com.daryaftmanazam.daryaftcore.dto.request.CustomerRequest;
import com.daryaftmanazam.daryaftcore.dto.response.CustomerResponse;
import com.daryaftmanazam.daryaftcore.dto.response.SubscriptionResponse;
import com.daryaftmanazam.daryaftcore.service.CustomerService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Customer management operations.
 * Provides endpoints for managing customers within businesses.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Add a new customer to a business.
     *
     * @param businessId Business ID
     * @param request    Customer creation request
     * @return Created customer with 201 status
     */
    @PostMapping("/business/{businessId}")
    @Operation(summary = "Add customer to business", description = "Creates a new customer associated with a specific business")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Customer created successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
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
    public ResponseEntity<ApiResponse<CustomerResponse>> addCustomer(
            @Parameter(description = "Business ID") @PathVariable Long businessId,
            @Valid @RequestBody CustomerRequest request) {
        log.info("REST: Adding new customer to business: {}", businessId);
        
        CustomerResponse response = customerService.addCustomer(businessId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<CustomerResponse>builder()
                        .success(true)
                        .message("Customer created successfully")
                        .data(response)
                        .build());
    }

    /**
     * Get customer details by ID.
     *
     * @param id Customer ID
     * @return Customer details with 200 status
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves detailed information about a specific customer")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer found",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.info("REST: Fetching customer with id: {}", id);
        
        CustomerResponse response = customerService.getCustomerById(id);
        
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Update customer information.
     *
     * @param id      Customer ID
     * @param request Customer update request
     * @return Updated customer with 200 status
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates an existing customer's information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        log.info("REST: Updating customer with id: {}", id);
        
        CustomerResponse response = customerService.updateCustomer(id, request);
        
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer updated successfully")
                .data(response)
                .build());
    }

    /**
     * Delete (soft delete) a customer.
     *
     * @param id Customer ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Soft deletes a customer (sets isActive to false)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Customer deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.info("REST: Deleting customer with id: {}", id);
        
        customerService.deleteCustomer(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * List customers for a business with pagination.
     *
     * @param businessId Business ID
     * @param page       Page number (0-indexed)
     * @param size       Page size
     * @param sortBy     Sort field
     * @param sortDir    Sort direction (asc/desc)
     * @return Paginated list of customers with 200 status
     */
    @GetMapping("/business/{businessId}")
    @Operation(summary = "List customers by business", description = "Retrieves a paginated list of customers for a specific business")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> getCustomersByBusiness(
            @Parameter(description = "Business ID") @PathVariable Long businessId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("REST: Fetching customers for business: {} - page: {}, size: {}", businessId, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerResponse> customerPage = customerService.getCustomersByBusiness(businessId, pageable);
        
        PageResponse<CustomerResponse> pageResponse = PageResponse.<CustomerResponse>builder()
                .content(customerPage.getContent())
                .pageNumber(customerPage.getNumber())
                .pageSize(customerPage.getSize())
                .totalElements(customerPage.getTotalElements())
                .totalPages(customerPage.getTotalPages())
                .isLast(customerPage.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.<PageResponse<CustomerResponse>>builder()
                .success(true)
                .message("Customers retrieved successfully")
                .data(pageResponse)
                .build());
    }

    /**
     * Search customers by name or phone number.
     *
     * @param businessId Business ID
     * @param keyword    Search keyword
     * @return List of matching customers with 200 status
     */
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Searches customers by name or phone number within a business")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            )
    })
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> searchCustomers(
            @Parameter(description = "Business ID") @RequestParam Long businessId,
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.info("REST: Searching customers in business {} with keyword: {}", businessId, keyword);
        
        List<CustomerResponse> customers = customerService.searchCustomers(businessId, keyword);
        
        return ResponseEntity.ok(ApiResponse.<List<CustomerResponse>>builder()
                .success(true)
                .message("Search completed successfully")
                .data(customers)
                .build());
    }

    /**
     * Get customer subscription history.
     *
     * @param id Customer ID
     * @return List of customer subscriptions with 200 status
     */
    @GetMapping("/{id}/subscriptions")
    @Operation(summary = "Get customer subscriptions", description = "Retrieves all subscriptions for a specific customer")
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
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.info("REST: Fetching subscriptions for customer: {}", id);
        
        List<SubscriptionResponse> subscriptions = customerService.getCustomerSubscriptionHistory(id);
        
        return ResponseEntity.ok(ApiResponse.<List<SubscriptionResponse>>builder()
                .success(true)
                .message("Subscriptions retrieved successfully")
                .data(subscriptions)
                .build());
    }
}
