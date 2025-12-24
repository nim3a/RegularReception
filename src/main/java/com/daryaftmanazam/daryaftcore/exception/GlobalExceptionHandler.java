package com.daryaftmanazam.daryaftcore.exception;

import com.daryaftmanazam.daryaftcore.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Handles all exceptions thrown by REST controllers and provides consistent error responses.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    
    private final MessageSource messageSource;
    
    /**
     * Get localized message from message source.
     */
    private String getMessage(String key) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }
    
    /**
     * Get localized message with arguments.
     */
    private String getMessage(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return key;
        }
    }
    
    @ExceptionHandler(BusinessNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBusinessNotFoundException(
            BusinessNotFoundException ex, HttpServletRequest request) {
        log.warn("Business not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("BUSINESS_NOT_FOUND")
                .message(getMessage("error.business.notfound"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
            CustomerNotFoundException ex, HttpServletRequest request) {
        log.warn("Customer not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("CUSTOMER_NOT_FOUND")
                .message(getMessage("error.customer.notfound"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(PaymentPlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentPlanNotFoundException(
            PaymentPlanNotFoundException ex, HttpServletRequest request) {
        log.warn("Payment plan not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("PAYMENT_PLAN_NOT_FOUND")
                .message(getMessage("error.paymentplan.notfound"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionNotFoundException(
            SubscriptionNotFoundException ex, HttpServletRequest request) {
        log.warn("Subscription not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("SUBSCRIPTION_NOT_FOUND")
                .message(getMessage("error.subscription.notfound"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(
            PaymentNotFoundException ex, HttpServletRequest request) {
        log.warn("Payment not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .errorCode("PAYMENT_NOT_FOUND")
                .message(getMessage("error.payment.notfound"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessingException(
            PaymentProcessingException ex, HttpServletRequest request) {
        log.error("Payment processing failed: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("PAYMENT_PROCESSING_FAILED")
                .message(getMessage("error.payment.processing"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(
            InvalidOperationException ex, HttpServletRequest request) {
        log.warn("Invalid operation: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("INVALID_OPERATION")
                .message(getMessage("error.operation.invalid"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_FAILED")
                .message(getMessage("error.validation.failed"))
                .fieldErrors(ex.getFieldErrors())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InvalidSubscriptionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSubscriptionException(
            InvalidSubscriptionException ex, HttpServletRequest request) {
        log.warn("Invalid subscription: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("INVALID_SUBSCRIPTION")
                .message(getMessage("error.subscription.invalid"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentException(
            InvalidPaymentException ex, HttpServletRequest request) {
        log.warn("Invalid payment: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("INVALID_PAYMENT")
                .message(getMessage("error.payment.invalid"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request: {}", request.getRequestURI());
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_FAILED")
                .message(getMessage("error.validation.failed"))
                .fieldErrors(fieldErrors)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle data integrity violations (e.g., duplicate entries, constraint violations).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = getMessage("error.data.conflict");
        
        // Try to provide more specific error message
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique") || ex.getMessage().contains("duplicate")) {
                message = getMessage("error.data.duplicate");
            } else if (ex.getMessage().contains("foreign key")) {
                message = getMessage("error.data.reference");
            }
        }
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .errorCode("DATA_CONFLICT")
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Handle type mismatch errors (e.g., invalid enum values, invalid path variables).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch: parameter '{}' with value '{}'", ex.getName(), ex.getValue());
        
        String message = String.format(getMessage("error.type.mismatch"), 
                ex.getValue(), ex.getName());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("TYPE_MISMATCH")
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("ILLEGAL_ARGUMENT")
                .message(getMessage("error.argument.illegal"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle rate limit exceeded exceptions.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<?> handleRateLimitExceeded(
            RateLimitExceededException ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded for request: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", ex.getMessage()));
    }
    
    /**
     * Handle access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for request: {} - {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "دسترسی به این منبع مجاز نیست"));
    }
    
    /**
     * Handle all other uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception occurred: ", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("INTERNAL_SERVER_ERROR")
                .message(getMessage("error.internal"))
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
