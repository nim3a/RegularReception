/**
 * Model package containing JPA entities and domain models.
 * 
 * Main entities:
 * - Business: Represents a business with customers and payment plans
 * - Customer: Represents a customer belonging to a business
 * - PaymentPlan: Defines payment terms and periods
 * - Subscription: Links customers to payment plans
 * - Payment: Records individual payments for subscriptions
 * - Notification: Manages customer notifications
 * 
 * All entities use JPA auditing for automatic timestamp management.
 * Relationships are bidirectional with helper methods for managing associations.
 */
package com.daryaftmanazam.daryaftcore.model;

// Base package for domain entities
