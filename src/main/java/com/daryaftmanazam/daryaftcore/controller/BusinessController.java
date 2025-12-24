package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.dto.ApiResponse;
import com.daryaftmanazam.daryaftcore.dto.PageResponse;
import com.daryaftmanazam.daryaftcore.dto.request.BusinessRequest;
import com.daryaftmanazam.daryaftcore.dto.response.BusinessDashboardResponse;
import com.daryaftmanazam.daryaftcore.dto.response.BusinessResponse;
import com.daryaftmanazam.daryaftcore.service.BusinessService;
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

/**
 * REST Controller for Business management operations.
 * Provides endpoints for CRUD operations and business dashboard.
 */
@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Business Management", description = "APIs for managing businesses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusinessController {

    private final BusinessService businessService;

    /**
     * Create a new business.
     *
     * @param request Business creation request
     * @return Created business with 201 status
     */
    @PostMapping
    @Operation(summary = "Create a new business", description = "Creates a new business entity in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Business created successfully",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(
            @Valid @RequestBody BusinessRequest request) {
        log.info("REST: Creating new business - {}", request.getBusinessName());
        
        BusinessResponse response = businessService.createBusiness(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<BusinessResponse>builder()
                        .success(true)
                        .message("Business created successfully")
                        .data(response)
                        .build());
    }

    /**
     * Get business details by ID.
     *
     * @param id Business ID
     * @return Business details with 200 status
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get business by ID", description = "Retrieves detailed information about a specific business")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Business found",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            )
    })
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(
            @Parameter(description = "Business ID") @PathVariable Long id) {
        log.info("REST: Fetching business with id: {}", id);
        
        BusinessResponse response = businessService.getBusinessById(id);
        
        return ResponseEntity.ok(ApiResponse.<BusinessResponse>builder()
                .success(true)
                .message("Business retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Update business information.
     *
     * @param id      Business ID
     * @param request Business update request
     * @return Updated business with 200 status
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update business", description = "Updates an existing business's information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Business updated successfully",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @Parameter(description = "Business ID") @PathVariable Long id,
            @Valid @RequestBody BusinessRequest request) {
        log.info("REST: Updating business with id: {}", id);
        
        BusinessResponse response = businessService.updateBusiness(id, request);
        
        return ResponseEntity.ok(ApiResponse.<BusinessResponse>builder()
                .success(true)
                .message("Business updated successfully")
                .data(response)
                .build());
    }

    /**
     * Delete (soft delete) a business.
     *
     * @param id Business ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete business", description = "Soft deletes a business (sets isActive to false)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Business deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            )
    })
    public ResponseEntity<Void> deleteBusiness(
            @Parameter(description = "Business ID") @PathVariable Long id) {
        log.info("REST: Deleting business with id: {}", id);
        
        businessService.deleteBusiness(id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * List all businesses with pagination.
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @param sortBy Sort field
     * @param sortDir Sort direction (asc/desc)
     * @return Paginated list of businesses with 200 status
     */
    @GetMapping
    @Operation(summary = "List all businesses", description = "Retrieves a paginated list of all businesses")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Businesses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<BusinessResponse>>> getAllBusinesses(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("REST: Fetching all businesses - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BusinessResponse> businessPage = businessService.getAllBusinesses(pageable);
        
        PageResponse<BusinessResponse> pageResponse = PageResponse.<BusinessResponse>builder()
                .content(businessPage.getContent())
                .pageNumber(businessPage.getNumber())
                .pageSize(businessPage.getSize())
                .totalElements(businessPage.getTotalElements())
                .totalPages(businessPage.getTotalPages())
                .isLast(businessPage.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.<PageResponse<BusinessResponse>>builder()
                .success(true)
                .message("Businesses retrieved successfully")
                .data(pageResponse)
                .build());
    }

    /**
     * Get business dashboard statistics.
     *
     * @param id Business ID
     * @return Dashboard statistics with 200 status
     */
    @GetMapping("/{id}/dashboard")
    @Operation(summary = "Get business dashboard", description = "Retrieves comprehensive dashboard statistics for a business")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Dashboard retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BusinessDashboardResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Business not found"
            )
    })
    public ResponseEntity<ApiResponse<BusinessDashboardResponse>> getBusinessDashboard(
            @Parameter(description = "Business ID") @PathVariable Long id) {
        log.info("REST: Fetching dashboard for business: {}", id);
        
        BusinessDashboardResponse response = businessService.getBusinessDashboard(id);
        
        return ResponseEntity.ok(ApiResponse.<BusinessDashboardResponse>builder()
                .success(true)
                .message("Dashboard retrieved successfully")
                .data(response)
                .build());
    }
}
