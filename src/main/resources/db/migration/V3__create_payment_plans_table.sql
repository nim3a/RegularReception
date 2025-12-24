-- Create payment plans table
CREATE TABLE payment_plans (
    id BIGSERIAL PRIMARY KEY,
    business_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    duration_months INT NOT NULL CHECK (duration_months > 0),
    discount_percentage DECIMAL(5,2) DEFAULT 0 CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    late_fee_percentage DECIMAL(5,2) DEFAULT 0 CHECK (late_fee_percentage >= 0),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_business FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);

-- Create index for business lookups
CREATE INDEX idx_payment_plans_business_id ON payment_plans(business_id);
