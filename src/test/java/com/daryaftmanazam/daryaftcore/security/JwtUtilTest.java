package com.daryaftmanazam.daryaftcore.security;

import com.daryaftmanazam.daryaftcore.model.Business;
import com.daryaftmanazam.daryaftcore.model.User;
import com.daryaftmanazam.daryaftcore.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtUtil
 * Tests JWT token generation, validation, and claim extraction
 */
@DisplayName("JWT Utility Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LW1pbmltdW0tMjU2LWJpdHMtbG9uZy1mb3ItaHMyNTYtYWxnb3JpdGhtLXRlc3Rpbmc=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // Set test secret using reflection (Base64 encoded, 256+ bits)
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 86400000L); // 24 hours

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole(UserRole.BUSINESS_ADMIN);

        Business business = new Business();
        business.setId(100L);
        testUser.setBusiness(business);
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken_CreatesValidToken() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername_Success() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void testExtractUserId_Success() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Long userId = jwtUtil.extractUserId(token);

        // Then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should extract business ID from token")
    void testExtractBusinessId_Success() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Long businessId = jwtUtil.extractBusinessId(token);

        // Then
        assertThat(businessId).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should extract role from token")
    void testExtractRole_Success() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String role = jwtUtil.extractRole(token);

        // Then
        assertThat(role).isEqualTo("BUSINESS_ADMIN");
    }

    @Test
    @DisplayName("Should validate token with matching username")
    void testValidateToken_ValidToken_ReturnsTrue() {
        // Given
        String token = jwtUtil.generateToken(testUser);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password123")
                .authorities("ROLE_BUSINESS_ADMIN")
                .build();

        // When
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate token with mismatched username")
    void testValidateToken_MismatchedUsername_ReturnsFalse() {
        // Given
        String token = jwtUtil.generateToken(testUser);
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("wronguser")
                .password("password123")
                .authorities("ROLE_BUSINESS_ADMIN")
                .build();

        // When
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate refresh token")
    void testGenerateRefreshToken_Success() {
        // When
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from refresh token")
    void testExtractUsernameFromRefreshToken() {
        // Given
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        // When
        String username = jwtUtil.extractUsername(refreshToken);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should handle user without business")
    void testGenerateToken_UserWithoutBusiness() {
        // Given - User without business
        User customerUser = new User();
        customerUser.setId(2L);
        customerUser.setUsername("customer");
        customerUser.setEmail("customer@example.com");
        customerUser.setRole(UserRole.CUSTOMER);
        customerUser.setBusiness(null);

        // When
        String token = jwtUtil.generateToken(customerUser);

        // Then
        assertThat(token).isNotNull();
        Long businessId = jwtUtil.extractBusinessId(token);
        assertThat(businessId).isNull();
    }

    @Test
    @DisplayName("Should handle different user roles")
    void testGenerateToken_DifferentRoles() {
        // Given - System admin
        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserRole.SYSTEM_ADMIN);

        // When
        String token = jwtUtil.generateToken(adminUser);
        String role = jwtUtil.extractRole(token);

        // Then
        assertThat(role).isEqualTo("SYSTEM_ADMIN");
    }

    @Test
    @DisplayName("Should throw exception for invalid token format")
    void testExtractUsername_InvalidToken_ThrowsException() {
        // Given - Invalid token
        String invalidToken = "invalid.token.format";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception for tampered token")
    void testValidateToken_TamperedToken_ThrowsException() {
        // Given
        String token = jwtUtil.generateToken(testUser);
        // Tamper with the token
        String tamperedToken = token.substring(0, token.length() - 10) + "tampered12";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(tamperedToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateToken_DifferentUsers_DifferentTokens() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setRole(UserRole.BUSINESS_ADMIN);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setRole(UserRole.CUSTOMER);

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Should extract all claims correctly")
    void testExtractAllClaims() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String username = jwtUtil.extractUsername(token);
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        Long businessId = jwtUtil.extractBusinessId(token);

        // Then
        assertThat(username).isEqualTo("testuser");
        assertThat(userId).isEqualTo(1L);
        assertThat(role).isEqualTo("BUSINESS_ADMIN");
        assertThat(businessId).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should handle expired token correctly")
    void testIsTokenExpired() {
        // Given - Create token with very short expiration
        JwtUtil shortExpiryJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "expiration", 1L); // 1 millisecond

        String token = shortExpiryJwtUtil.generateToken(testUser);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When & Then - Expired token should fail validation
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password123")
                .authorities("ROLE_BUSINESS_ADMIN")
                .build();

        boolean isValid = shortExpiryJwtUtil.isTokenValid(token, userDetails);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle null business gracefully")
    void testExtractBusinessId_NullBusiness() {
        // Given - User without business
        User userWithoutBusiness = new User();
        userWithoutBusiness.setId(5L);
        userWithoutBusiness.setUsername("nobusiness");
        userWithoutBusiness.setEmail("nobusiness@example.com");
        userWithoutBusiness.setRole(UserRole.CUSTOMER);
        userWithoutBusiness.setBusiness(null);

        // When
        String token = jwtUtil.generateToken(userWithoutBusiness);
        Long businessId = jwtUtil.extractBusinessId(token);

        // Then
        assertThat(businessId).isNull();
    }
}
