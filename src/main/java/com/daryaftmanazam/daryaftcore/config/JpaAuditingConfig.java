package com.daryaftmanazam.daryaftcore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class to enable JPA auditing for automatic timestamp management.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
