-- Add SMS reminder tracking to subscriptions table
-- This migration adds a timestamp column to track when the last reminder SMS was sent

-- Add last_reminder_sent column
ALTER TABLE subscriptions
ADD COLUMN IF NOT EXISTS last_reminder_sent TIMESTAMP;

-- Create indexes for performance optimization
-- Index on end_date for finding expiring subscriptions
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date 
ON subscriptions(end_date);

-- Composite index on status and end_date for active subscriptions queries
CREATE INDEX IF NOT EXISTS idx_subscriptions_status_end_date 
ON subscriptions(status, end_date);

-- Comment on the new column
COMMENT ON COLUMN subscriptions.last_reminder_sent IS 
'Timestamp when the last SMS reminder was sent for this subscription';
