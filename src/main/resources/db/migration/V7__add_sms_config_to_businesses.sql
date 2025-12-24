-- Add SMS configuration columns to businesses table
ALTER TABLE businesses ADD COLUMN sms_enabled BOOLEAN DEFAULT false;
ALTER TABLE businesses ADD COLUMN sms_api_key VARCHAR(255);
ALTER TABLE businesses ADD COLUMN sms_sender_number VARCHAR(20);
ALTER TABLE businesses ADD COLUMN sms_provider VARCHAR(50) DEFAULT 'MELIPAYAMAK';
