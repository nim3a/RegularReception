/**
 * Exception handling package for the Daryaft Core application.
 * <p>
 * This package contains custom exception classes and global exception handlers
 * for consistent error handling across the application.
 * </p>
 * 
 * <h2>Custom Exceptions</h2>
 * <ul>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.BusinessNotFoundException} - 
 *       Thrown when a business entity is not found (404)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.CustomerNotFoundException} - 
 *       Thrown when a customer entity is not found (404)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.PaymentPlanNotFoundException} - 
 *       Thrown when a payment plan is not found (404)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.SubscriptionNotFoundException} - 
 *       Thrown when a subscription is not found (404)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.PaymentNotFoundException} - 
 *       Thrown when a payment is not found (404)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.PaymentProcessingException} - 
 *       Thrown when payment processing fails (400)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.InvalidOperationException} - 
 *       Thrown when an invalid operation is attempted (400)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.ValidationException} - 
 *       Thrown when validation fails (400)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.InvalidSubscriptionException} - 
 *       Thrown when a subscription is invalid (400)</li>
 *   <li>{@link com.daryaftmanazam.daryaftcore.exception.InvalidPaymentException} - 
 *       Thrown when a payment is invalid (400)</li>
 * </ul>
 * 
 * <h2>Global Exception Handler</h2>
 * <p>
 * {@link com.daryaftmanazam.daryaftcore.exception.GlobalExceptionHandler} handles all 
 * exceptions thrown by REST controllers and provides consistent error responses with:
 * </p>
 * <ul>
 *   <li>Persian error messages from message source</li>
 *   <li>Appropriate HTTP status codes (404, 400, 409, 500)</li>
 *   <li>Structured error responses with ErrorResponse DTO</li>
 *   <li>Request path and timestamp information</li>
 *   <li>Field-level validation errors when applicable</li>
 *   <li>Proper logging at appropriate levels (ERROR, WARN)</li>
 * </ul>
 * 
 * <h2>Error Response Structure</h2>
 * <p>
 * All error responses follow a consistent structure defined by ErrorResponse DTO:
 * </p>
 * <pre>
 * {
 *   "status": 404,
 *   "error_code": "BUSINESS_NOT_FOUND",
 *   "message": "کسب‌وکار یافت نشد",
 *   "path": "/api/businesses/123",
 *   "timestamp": "2025-12-22T10:30:45"
 * }
 * </pre>
 * 
 * @since 1.0
 * @version 1.0
 */
package com.daryaftmanazam.daryaftcore.exception;
