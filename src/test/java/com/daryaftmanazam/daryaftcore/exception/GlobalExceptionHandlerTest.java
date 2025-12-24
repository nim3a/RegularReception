package com.daryaftmanazam.daryaftcore.exception;

import com.daryaftmanazam.daryaftcore.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Autowired
    private MessageSource messageSource;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
    }

    @Test
    @DisplayName("Should handle BusinessNotFoundException with 404")
    void testHandleBusinessNotFoundException() {
        // Given
        BusinessNotFoundException exception = new BusinessNotFoundException(1L);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleBusinessNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getErrorCode()).isEqualTo("BUSINESS_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isNotBlank();
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("Should handle CustomerNotFoundException with 404")
    void testHandleCustomerNotFoundException() {
        // Given
        CustomerNotFoundException exception = new CustomerNotFoundException(1L);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleCustomerNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getErrorCode()).isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle PaymentPlanNotFoundException with 404")
    void testHandlePaymentPlanNotFoundException() {
        // Given
        PaymentPlanNotFoundException exception = new PaymentPlanNotFoundException(1L);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handlePaymentPlanNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getErrorCode()).isEqualTo("PAYMENT_PLAN_NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle SubscriptionNotFoundException with 404")
    void testHandleSubscriptionNotFoundException() {
        // Given
        SubscriptionNotFoundException exception = new SubscriptionNotFoundException(1L);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleSubscriptionNotFoundException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getErrorCode()).isEqualTo("SUBSCRIPTION_NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle PaymentProcessingException with 400")
    void testHandlePaymentProcessingException() {
        // Given
        PaymentProcessingException exception = new PaymentProcessingException("Processing failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handlePaymentProcessingException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getErrorCode()).isEqualTo("PAYMENT_PROCESSING_FAILED");
    }

    @Test
    @DisplayName("Should handle InvalidOperationException with 400")
    void testHandleInvalidOperationException() {
        // Given
        InvalidOperationException exception = new InvalidOperationException("Invalid operation");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleInvalidOperationException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_OPERATION");
    }

    @Test
    @DisplayName("Should handle ValidationException with 400 and field errors")
    void testHandleValidationException() {
        // Given
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("email", "Invalid email format");
        fieldErrors.put("name", "Name is required");
        ValidationException exception = new ValidationException("Validation failed", fieldErrors);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleValidationException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().getFieldErrors()).hasSize(2);
        assertThat(response.getBody().getFieldErrors()).containsEntry("email", "Invalid email format");
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException with 409")
    void testHandleDataIntegrityViolationException() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Duplicate entry");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleDataIntegrityViolationException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getErrorCode()).isEqualTo("DATA_CONFLICT");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400")
    void testHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleIllegalArgumentException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ILLEGAL_ARGUMENT");
    }

    @Test
    @DisplayName("Should handle generic Exception with 500")
    void testHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler
                .handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("Should return Persian error messages")
    void testPersianErrorMessages() {
        // Given
        Locale persianLocale = new Locale("fa", "IR");

        // When
        String businessNotFound = messageSource.getMessage("error.business.notfound", null, persianLocale);
        String customerNotFound = messageSource.getMessage("error.customer.notfound", null, persianLocale);
        String subscriptionInvalid = messageSource.getMessage("error.subscription.invalid", null, persianLocale);
        String validationFailed = messageSource.getMessage("error.validation.failed", null, persianLocale);

        // Then
        assertThat(businessNotFound).isEqualTo("کسب‌وکار یافت نشد");
        assertThat(customerNotFound).isEqualTo("مشتری یافت نشد");
        assertThat(subscriptionInvalid).isEqualTo("اشتراک نامعتبر است");
        assertThat(validationFailed).isEqualTo("اعتبارسنجی ناموفق بود");
    }
}
