package com.daryaftmanazam.daryaftcore.model.enums;

/**
 * User roles for role-based access control
 */
public enum UserRole {
    SUPER_ADMIN,    // Full system access
    BUSINESS_ADMIN, // Manage own business
    CUSTOMER        // View own subscriptions
}
