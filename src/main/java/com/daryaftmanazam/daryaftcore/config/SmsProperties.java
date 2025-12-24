package com.daryaftmanazam.daryaftcore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * SMS Configuration Properties
 * 
 * Binds SMS-related configuration from application.yml
 * Includes provider settings and message templates
 */
@Configuration
@ConfigurationProperties(prefix = "sms")
@Data
public class SmsProperties {
    
    /**
     * SMS provider name (e.g., "melipayamak")
     */
    private String provider;
    
    /**
     * MeliPayamak provider configuration
     */
    private MeliPayamakConfig melipayamak;
    
    /**
     * Message templates for different notification types
     * Keys: reminder, payment-success, payment-failed, subscription-created
     */
    private Map<String, String> templates;

    /**
     * MeliPayamak SMS provider configuration
     */
    @Data
    public static class MeliPayamakConfig {
        /**
         * CRITICAL: Must be false by default to prevent accidental SMS sends
         */
        private boolean enabled;
        
        /**
         * API key for MeliPayamak service
         */
        private String apiKey;
        
        /**
         * Username for authentication
         */
        private String username;
        
        /**
         * Password for authentication
         */
        private String password;
        
        /**
         * SMS line number (sender ID)
         */
        private String lineNumber;
    }

    /**
     * Get a template by key with fallback to empty string
     * 
     * @param key Template key (e.g., "reminder", "payment-success")
     * @return Template string or empty string if not found
     */
    public String getTemplate(String key) {
        return templates != null ? templates.getOrDefault(key, "") : "";
    }
}
