package com.daryaftmanazam.daryaftcore.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration for Bean Validation.
 * Provides a validator bean for manual validation when needed.
 */
@Configuration
public class ValidationConfig {

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
}
