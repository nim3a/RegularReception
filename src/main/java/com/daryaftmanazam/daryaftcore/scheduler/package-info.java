/**
 * Scheduler package containing scheduled tasks for automation.
 * 
 * <p>This package includes scheduled components that handle automated background operations:
 * <ul>
 *   <li>SubscriptionScheduler - Manages subscription lifecycle (overdue, expired)</li>
 *   <li>NotificationScheduler - Handles automated notifications and reminders</li>
 * </ul>
 * 
 * <p>All schedulers use Spring's @Scheduled annotation for task automation.
 * Scheduling is enabled via @EnableScheduling in the main application class.
 */
package com.daryaftmanazam.daryaftcore.scheduler;
