-- SMS Configuration Database Migration
-- This script adds SMS configuration columns to the businesses table
-- These changes will be automatically applied by Hibernate when ddl-auto: update is set
-- This file is provided for reference and manual database migrations

-- ========================================
-- Add SMS Configuration Columns
-- ========================================

-- SMS Provider name (e.g., 'melipayamak', 'kavenegar')
ALTER TABLE businesses 
ADD COLUMN sms_provider VARCHAR(50);

-- API Key for SMS provider
ALTER TABLE businesses 
ADD COLUMN sms_api_key VARCHAR(200);

-- Username for SMS provider account
ALTER TABLE businesses 
ADD COLUMN sms_username VARCHAR(100);

-- Password for SMS provider account
ALTER TABLE businesses 
ADD COLUMN sms_password VARCHAR(100);

-- SMS line number (sender number)
ALTER TABLE businesses 
ADD COLUMN sms_line_number VARCHAR(20);

-- SMS enabled flag (default: false)
ALTER TABLE businesses 
ADD COLUMN sms_enabled BOOLEAN DEFAULT FALSE;

-- ========================================
-- Comments (H2 Syntax)
-- ========================================

COMMENT ON COLUMN businesses.sms_provider IS 'SMS provider name (e.g., melipayamak, kavenegar)';
COMMENT ON COLUMN businesses.sms_api_key IS 'API key for the SMS provider';
COMMENT ON COLUMN businesses.sms_username IS 'Username for SMS provider account';
COMMENT ON COLUMN businesses.sms_password IS 'Password for SMS provider account';
COMMENT ON COLUMN businesses.sms_line_number IS 'SMS line number (sender number)';
COMMENT ON COLUMN businesses.sms_enabled IS 'Whether SMS is enabled for this business';

-- ========================================
-- Example Data (Optional)
-- ========================================

-- Example: Enable SMS for a specific business
-- UPDATE businesses 
-- SET sms_provider = 'melipayamak',
--     sms_api_key = 'c2d0e69c-2d62-488c-82ee-16180dd56c1b',
--     sms_username = 'your-username',
--     sms_password = 'your-password',
--     sms_line_number = '5000xxxxx',
--     sms_enabled = TRUE
-- WHERE id = 1;

-- ========================================
-- Rollback Script (If Needed)
-- ========================================

-- To remove SMS configuration columns (not recommended after data is added):
-- ALTER TABLE businesses DROP COLUMN sms_provider;
-- ALTER TABLE businesses DROP COLUMN sms_api_key;
-- ALTER TABLE businesses DROP COLUMN sms_username;
-- ALTER TABLE businesses DROP COLUMN sms_password;
-- ALTER TABLE businesses DROP COLUMN sms_line_number;
-- ALTER TABLE businesses DROP COLUMN sms_enabled;

-- ========================================
-- Notes
-- ========================================
-- 1. These columns are managed by JPA/Hibernate with @Embedded SmsConfig
-- 2. Hibernate will automatically create these columns when ddl-auto: update is set
-- 3. All columns are nullable to support businesses without SMS configuration
-- 4. SMS is disabled by default (sms_enabled = FALSE)
-- 5. Sensitive data (password, API key) should be encrypted in production
