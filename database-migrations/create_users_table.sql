-- Create users table for authentication and authorization
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    business_id BIGINT,
    customer_id BIGINT,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    CONSTRAINT check_role CHECK (role IN ('SUPER_ADMIN', 'BUSINESS_ADMIN', 'CUSTOMER')),
    CONSTRAINT fk_users_business FOREIGN KEY (business_id) REFERENCES businesses(id),
    CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_business ON users(business_id);
CREATE INDEX IF NOT EXISTS idx_users_customer ON users(customer_id);

-- Insert default super admin user
-- Username: admin
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (username, email, password, role, enabled) VALUES
('admin', 'admin@daryaftmanzom.ir', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SUPER_ADMIN', true);
