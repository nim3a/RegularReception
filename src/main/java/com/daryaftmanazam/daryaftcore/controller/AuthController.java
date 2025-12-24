package com.daryaftmanazam.daryaftcore.controller;

import com.daryaftmanazam.daryaftcore.annotation.RateLimit;
import com.daryaftmanazam.daryaftcore.dto.request.LoginRequest;
import com.daryaftmanazam.daryaftcore.dto.request.RefreshTokenRequest;
import com.daryaftmanazam.daryaftcore.dto.request.RegisterRequest;
import com.daryaftmanazam.daryaftcore.dto.response.AuthResponse;
import com.daryaftmanazam.daryaftcore.model.User;
import com.daryaftmanazam.daryaftcore.model.enums.UserRole;
import com.daryaftmanazam.daryaftcore.repository.UserRepository;
import com.daryaftmanazam.daryaftcore.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Authentication controller for login, registration, and token management
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "مدیریت احراز هویت و دسترسی")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @RateLimit(capacity = 5, refillTokens = 5, refillDuration = 1, refillUnit = TimeUnit.MINUTES)
    @Operation(summary = "ورود کاربر", description = "ورود به سیستم با نام کاربری و رمز عبور")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            log.info("User {} logged in successfully", user.getUsername());

            return ResponseEntity.ok(new AuthResponse(
                    token,
                    refreshToken,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getBusiness() != null ? user.getBusiness().getId() : null,
                    user.getCustomer() != null ? user.getCustomer().getId() : null
            ));
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "نام کاربری یا رمز عبور اشتباه است"));
        }
    }

    @PostMapping("/register")
    @RateLimit(capacity = 3, refillTokens = 3, refillDuration = 1, refillUnit = TimeUnit.HOURS)
    @Operation(summary = "ثبت‌نام کاربر", description = "ایجاد حساب کاربری جدید")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "نام کاربری تکراری است"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ایمیل تکراری است"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setEnabled(true);

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        return ResponseEntity.ok(Map.of("message", "ثبت‌نام با موفقیت انجام شد"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "تازه‌سازی توکن", description = "دریافت توکن جدید با استفاده از refresh token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String username = jwtUtil.extractUsername(request.getRefreshToken());
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

            String newToken = jwtUtil.generateToken(user);

            log.info("Token refreshed for user: {}", username);
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "توکن نامعتبر است"));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "اطلاعات کاربر فعلی", description = "دریافت اطلاعات کاربر لاگین شده")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "businessId", user.getBusiness() != null ? user.getBusiness().getId() : null,
                "customerId", user.getCustomer() != null ? user.getCustomer().getId() : null
        ));
    }
}
