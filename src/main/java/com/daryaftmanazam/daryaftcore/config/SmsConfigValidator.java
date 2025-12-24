package com.daryaftmanazam.daryaftcore.config;

import com.daryaftmanazam.daryaftcore.service.sms.AccountBalance;
import com.daryaftmanazam.daryaftcore.service.sms.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * SMS Configuration Validator
 * 
 * Validates SMS configuration on application startup:
 * - Logs clear warning when SMS is enabled
 * - Checks provider connectivity and balance
 * - Provides safety mechanism to prevent accidental SMS sends
 */
@Component
@Slf4j
public class SmsConfigValidator implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired(required = false)
    private SmsProvider smsProvider;

    @Value("${sms.melipayamak.enabled:false}")
    private boolean smsEnabled;

    /**
     * Called when application is ready and fully initialized
     * Performs SMS configuration validation and logging
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (smsEnabled) {
            log.warn("⚠️ SMS SENDING IS ENABLED - Real SMS messages will be sent!");
            
            if (smsProvider != null) {
                try {
                    AccountBalance balance = smsProvider.getBalance();
                    log.info("SMS Provider connected. Balance: {} {}",
                            balance.getBalance(), balance.getCurrency());
                } catch (Exception e) {
                    log.error("Failed to connect to SMS provider", e);
                }
            } else {
                log.warn("SMS is enabled but no SMS provider bean found!");
            }
        } else {
            log.info("ℹ️ SMS SENDING IS DISABLED - Messages will only be logged");
        }
    }
}
