-- Create payments table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount >= 0),
    payment_date TIMESTAMP,
    transaction_id VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    late_fee DECIMAL(15,2) DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_payments_business_id ON payments(business_id);
CREATE INDEX idx_payments_subscription_id ON payments(subscription_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
