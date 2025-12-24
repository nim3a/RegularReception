-- Sample Data Initialization for Daryaft Core Application
-- This file contains test data for development and testing purposes

-- =============================================================================
-- BUSINESSES
-- =============================================================================

-- Business 1: باشگاه بی رانرز (Be Runners Gym)
INSERT INTO businesses (business_name, owner_name, contact_email, contact_phone, description, is_active, created_at, updated_at)
VALUES ('باشگاه بی رانرز', 'ویدا مختاری', 'info@berunners.ir', '09121234567', 'باشگاه ورزشی و دو', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Business 2: مجتمع مسکونی پارسیان (Parsian Residential Complex)
INSERT INTO businesses (business_name, owner_name, contact_email, contact_phone, description, is_active, created_at, updated_at)
VALUES ('مجتمع مسکونی پارسیان', 'محمد رضایی', 'manager@parsian.ir', '09131234567', 'مجتمع مسکونی با امکانات رفاهی', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================================================
-- PAYMENT PLANS FOR BUSINESS 1 (باشگاه بی رانرز)
-- =============================================================================

-- Monthly Plan - 500,000 Toman
INSERT INTO payment_plans (plan_name, period_type, period_count, base_amount, discount_percentage, late_fee_per_day, grace_period_days, is_active, business_id)
VALUES ('اشتراک ماهانه', 'MONTHLY', 1, 500000.00, 0.00, 10000.00, 3, true, 1);

-- Quarterly Plan - 1,350,000 Toman (10% discount)
INSERT INTO payment_plans (plan_name, period_type, period_count, base_amount, discount_percentage, late_fee_per_day, grace_period_days, is_active, business_id)
VALUES ('اشتراک سه ماهه', 'QUARTERLY', 3, 1500000.00, 10.00, 10000.00, 5, true, 1);

-- Semi-annual Plan - 2,550,000 Toman (15% discount)
INSERT INTO payment_plans (plan_name, period_type, period_count, base_amount, discount_percentage, late_fee_per_day, grace_period_days, is_active, business_id)
VALUES ('اشتراک شش ماهه', 'SEMI_ANNUAL', 6, 3000000.00, 15.00, 10000.00, 7, true, 1);

-- =============================================================================
-- PAYMENT PLANS FOR BUSINESS 2 (مجتمع مسکونی پارسیان)
-- =============================================================================

-- Monthly Plan - 500,000 Toman
INSERT INTO payment_plans (plan_name, period_type, period_count, base_amount, discount_percentage, late_fee_per_day, grace_period_days, is_active, business_id)
VALUES ('شارژ ماهانه', 'MONTHLY', 1, 500000.00, 0.00, 15000.00, 3, true, 2);

-- Quarterly Plan - 1,350,000 Toman (10% discount)
INSERT INTO payment_plans (plan_name, period_type, period_count, base_amount, discount_percentage, late_fee_per_day, grace_period_days, is_active, business_id)
VALUES ('شارژ سه ماهه', 'QUARTERLY', 3, 1500000.00, 10.00, 15000.00, 5, true, 2);

-- Semi-annual Plan - 2,550,000 Toman (15% discount)
INSERT INTO payment_plans (plan_name, period_type, period_count, base_amount, discount_percentage, late_fee_per_day, grace_period_days, is_active, business_id)
VALUES ('شارژ شش ماهه', 'SEMI_ANNUAL', 6, 3000000.00, 15.00, 15000.00, 7, true, 2);

-- =============================================================================
-- CUSTOMERS FOR BUSINESS 1 (باشگاه بی رانرز)
-- =============================================================================

-- Customer 1
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('علی', 'احمدی', '09121111111', 'ali.ahmadi@email.com', 'REGULAR', true, DATEADD('MONTH', -5, CURRENT_DATE), CURRENT_TIMESTAMP, 1);

-- Customer 2
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('زهرا', 'محمدی', '09122222222', 'zahra.mohammadi@email.com', 'VIP', true, DATEADD('MONTH', -4, CURRENT_DATE), CURRENT_TIMESTAMP, 1);

-- Customer 3
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('حسین', 'کریمی', '09123333333', 'hossein.karimi@email.com', 'REGULAR', true, DATEADD('MONTH', -3, CURRENT_DATE), CURRENT_TIMESTAMP, 1);

-- Customer 4
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('فاطمه', 'رضایی', '09124444444', 'fatemeh.rezaei@email.com', 'NEW', true, DATEADD('MONTH', -2, CURRENT_DATE), CURRENT_TIMESTAMP, 1);

-- Customer 5
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('مهدی', 'نوری', '09125555555', 'mahdi.noori@email.com', 'REGULAR', true, DATEADD('MONTH', -1, CURRENT_DATE), CURRENT_TIMESTAMP, 1);

-- =============================================================================
-- CUSTOMERS FOR BUSINESS 2 (مجتمع مسکونی پارسیان)
-- =============================================================================

-- Customer 6
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('سارا', 'حسینی', '09131111111', 'sara.hosseini@email.com', 'REGULAR', true, DATEADD('MONTH', -5, CURRENT_DATE), CURRENT_TIMESTAMP, 2);

-- Customer 7
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('رضا', 'مرادی', '09132222222', 'reza.moradi@email.com', 'VIP', true, DATEADD('MONTH', -4, CURRENT_DATE), CURRENT_TIMESTAMP, 2);

-- Customer 8
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('مریم', 'صادقی', '09133333333', 'maryam.sadeghi@email.com', 'REGULAR', true, DATEADD('MONTH', -3, CURRENT_DATE), CURRENT_TIMESTAMP, 2);

-- Customer 9
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('امیر', 'جعفری', '09134444444', 'amir.jafari@email.com', 'REGULAR', true, DATEADD('MONTH', -2, CURRENT_DATE), CURRENT_TIMESTAMP, 2);

-- Customer 10
INSERT INTO customers (first_name, last_name, phone_number, email, customer_type, is_active, join_date, created_at, business_id)
VALUES ('لیلا', 'باقری', '09135555555', 'leila.bagheri@email.com', 'NEW', true, DATEADD('MONTH', -1, CURRENT_DATE), CURRENT_TIMESTAMP, 2);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 1 - Customer 1 (علی احمدی)
-- =============================================================================

-- Active Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (1, 1, DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', 1, CURRENT_DATE), 'ACTIVE', 500000.00, 0.00, DATEADD('DAY', 15, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Expired Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (1, 2, DATEADD('MONTH', -5, CURRENT_DATE), DATEADD('MONTH', -2, CURRENT_DATE), 'EXPIRED', 1350000.00, 150000.00, DATEADD('MONTH', -2, CURRENT_DATE), CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 1 - Customer 2 (زهرا محمدی)
-- =============================================================================

-- Active Semi-annual Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (2, 3, DATEADD('MONTH', -4, CURRENT_DATE), DATEADD('MONTH', 2, CURRENT_DATE), 'ACTIVE', 2550000.00, 450000.00, DATEADD('DAY', 30, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Pending Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (2, 1, CURRENT_DATE, DATEADD('MONTH', 1, CURRENT_DATE), 'PENDING', 500000.00, 0.00, CURRENT_DATE, CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 1 - Customer 3 (حسین کریمی)
-- =============================================================================

-- Overdue Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (3, 2, DATEADD('MONTH', -3, CURRENT_DATE), DATEADD('MONTH', 0, CURRENT_DATE), 'OVERDUE', 1350000.00, 150000.00, DATEADD('DAY', -10, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Active Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (3, 1, DATEADD('MONTH', -1, CURRENT_DATE), DATEADD('MONTH', 2, CURRENT_DATE), 'ACTIVE', 500000.00, 0.00, DATEADD('DAY', 20, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Cancelled Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (3, 1, DATEADD('MONTH', -5, CURRENT_DATE), DATEADD('MONTH', -3, CURRENT_DATE), 'CANCELLED', 500000.00, 0.00, DATEADD('MONTH', -3, CURRENT_DATE), CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 1 - Customer 4 (فاطمه رضایی)
-- =============================================================================

-- Active Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (4, 1, DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', 1, CURRENT_DATE), 'ACTIVE', 500000.00, 0.00, DATEADD('DAY', 5, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Pending Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (4, 2, CURRENT_DATE, DATEADD('MONTH', 3, CURRENT_DATE), 'PENDING', 1350000.00, 150000.00, CURRENT_DATE, CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 1 - Customer 5 (مهدی نوری)
-- =============================================================================

-- Active Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (5, 2, DATEADD('MONTH', -1, CURRENT_DATE), DATEADD('MONTH', 2, CURRENT_DATE), 'ACTIVE', 1350000.00, 150000.00, DATEADD('MONTH', 1, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Overdue Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (5, 1, DATEADD('MONTH', -3, CURRENT_DATE), DATEADD('DAY', -5, CURRENT_DATE), 'OVERDUE', 500000.00, 0.00, DATEADD('DAY', -15, CURRENT_DATE), CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 2 - Customer 6 (سارا حسینی)
-- =============================================================================

-- Active Semi-annual Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (6, 6, DATEADD('MONTH', -5, CURRENT_DATE), DATEADD('MONTH', 1, CURRENT_DATE), 'ACTIVE', 2550000.00, 450000.00, DATEADD('DAY', 25, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Expired Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (6, 4, DATEADD('MONTH', -6, CURRENT_DATE), DATEADD('MONTH', -5, CURRENT_DATE), 'EXPIRED', 500000.00, 0.00, DATEADD('MONTH', -5, CURRENT_DATE), CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 2 - Customer 7 (رضا مرادی)
-- =============================================================================

-- Active Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (7, 5, DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', 1, CURRENT_DATE), 'ACTIVE', 1350000.00, 150000.00, DATEADD('DAY', 10, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Pending Semi-annual Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (7, 6, CURRENT_DATE, DATEADD('MONTH', 6, CURRENT_DATE), 'PENDING', 2550000.00, 450000.00, CURRENT_DATE, CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 2 - Customer 8 (مریم صادقی)
-- =============================================================================

-- Overdue Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (8, 4, DATEADD('MONTH', -3, CURRENT_DATE), DATEADD('DAY', 0, CURRENT_DATE), 'OVERDUE', 500000.00, 0.00, DATEADD('DAY', -8, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Active Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (8, 4, DATEADD('MONTH', -1, CURRENT_DATE), DATEADD('MONTH', 2, CURRENT_DATE), 'ACTIVE', 500000.00, 0.00, DATEADD('DAY', 18, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Cancelled Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (8, 5, DATEADD('MONTH', -5, CURRENT_DATE), DATEADD('MONTH', -3, CURRENT_DATE), 'CANCELLED', 1350000.00, 150000.00, DATEADD('MONTH', -3, CURRENT_DATE), CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 2 - Customer 9 (امیر جعفری)
-- =============================================================================

-- Active Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (9, 5, DATEADD('MONTH', -2, CURRENT_DATE), DATEADD('MONTH', 1, CURRENT_DATE), 'ACTIVE', 1350000.00, 150000.00, DATEADD('DAY', 12, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Pending Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (9, 4, CURRENT_DATE, DATEADD('MONTH', 1, CURRENT_DATE), 'PENDING', 500000.00, 0.00, CURRENT_DATE, CURRENT_TIMESTAMP);

-- =============================================================================
-- SUBSCRIPTIONS FOR BUSINESS 2 - Customer 10 (لیلا باقری)
-- =============================================================================

-- Active Monthly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (10, 4, DATEADD('MONTH', -1, CURRENT_DATE), DATEADD('MONTH', 2, CURRENT_DATE), 'ACTIVE', 500000.00, 0.00, DATEADD('DAY', 22, CURRENT_DATE), CURRENT_TIMESTAMP);

-- Overdue Quarterly Subscription
INSERT INTO subscriptions (customer_id, payment_plan_id, start_date, end_date, status, total_amount, discount_applied, next_payment_date, created_at)
VALUES (10, 5, DATEADD('MONTH', -4, CURRENT_DATE), DATEADD('DAY', -3, CURRENT_DATE), 'OVERDUE', 1350000.00, 150000.00, DATEADD('DAY', -12, CURRENT_DATE), CURRENT_TIMESTAMP);

-- =============================================================================
-- PAYMENT RECORDS
-- =============================================================================

-- Payments for Subscription 1 (Customer 1 - Active Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (1, 500000.00, DATEADD('MONTH', -2, CURRENT_TIMESTAMP), DATEADD('MONTH', -2, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-001-001', 0.00, 'پرداخت اولیه');

INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (1, 500000.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), DATEADD('MONTH', -1, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-001-002', 0.00, 'پرداخت ماهانه');

-- Payments for Subscription 2 (Customer 1 - Expired Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (2, 1350000.00, DATEADD('MONTH', -5, CURRENT_TIMESTAMP), DATEADD('MONTH', -5, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-002-001', 0.00, 'پرداخت کامل اشتراک سه ماهه');

-- Payments for Subscription 3 (Customer 2 - Active Semi-annual)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (3, 2550000.00, DATEADD('MONTH', -4, CURRENT_TIMESTAMP), DATEADD('MONTH', -4, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-003-001', 0.00, 'پرداخت کامل اشتراک شش ماهه');

-- Payments for Subscription 4 (Customer 2 - Pending Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (4, 500000.00, NULL, CURRENT_DATE, 'PENDING', NULL, 'TXN-004-001', 0.00, 'در انتظار پرداخت');

-- Payments for Subscription 5 (Customer 3 - Overdue Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (5, 1350000.00, DATEADD('MONTH', -3, CURRENT_TIMESTAMP), DATEADD('MONTH', -3, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-005-001', 0.00, 'پرداخت اولیه');

INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (5, 450000.00, NULL, DATEADD('DAY', -10, CURRENT_DATE), 'PENDING', NULL, 'TXN-005-002', 100000.00, 'پرداخت معوقه با جریمه دیرکرد');

-- Payments for Subscription 6 (Customer 3 - Active Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (6, 500000.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), DATEADD('MONTH', -1, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-006-001', 0.00, 'پرداخت ماهانه');

-- Payments for Subscription 7 (Customer 3 - Cancelled)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (7, 500000.00, DATEADD('MONTH', -5, CURRENT_TIMESTAMP), DATEADD('MONTH', -5, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-007-001', 0.00, 'پرداخت قبل از لغو');

-- Payments for Subscription 8 (Customer 4 - Active Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (8, 500000.00, DATEADD('MONTH', -2, CURRENT_TIMESTAMP), DATEADD('MONTH', -2, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-008-001', 0.00, 'پرداخت اولیه');

INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (8, 500000.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), DATEADD('MONTH', -1, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-008-002', 0.00, 'پرداخت ماهانه');

-- Payments for Subscription 9 (Customer 4 - Pending Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (9, 1350000.00, NULL, CURRENT_DATE, 'PENDING', NULL, 'TXN-009-001', 0.00, 'در انتظار پرداخت');

-- Payments for Subscription 10 (Customer 5 - Active Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (10, 1350000.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), DATEADD('MONTH', -1, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-010-001', 0.00, 'پرداخت کامل اشتراک سه ماهه');

-- Payments for Subscription 11 (Customer 5 - Overdue Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (11, 500000.00, DATEADD('MONTH', -3, CURRENT_TIMESTAMP), DATEADD('MONTH', -3, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-011-001', 0.00, 'پرداخت اولیه');

INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (11, 500000.00, NULL, DATEADD('DAY', -15, CURRENT_DATE), 'PENDING', NULL, 'TXN-011-002', 150000.00, 'پرداخت معوقه با جریمه دیرکرد');

-- Payments for Subscription 12 (Customer 6 - Active Semi-annual)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (12, 2550000.00, DATEADD('MONTH', -5, CURRENT_TIMESTAMP), DATEADD('MONTH', -5, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-012-001', 0.00, 'پرداخت کامل اشتراک شش ماهه');

-- Payments for Subscription 13 (Customer 6 - Expired Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (13, 500000.00, DATEADD('MONTH', -6, CURRENT_TIMESTAMP), DATEADD('MONTH', -6, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-013-001', 0.00, 'پرداخت قبل از انقضا');

-- Payments for Subscription 14 (Customer 7 - Active Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (14, 1350000.00, DATEADD('MONTH', -2, CURRENT_TIMESTAMP), DATEADD('MONTH', -2, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-014-001', 0.00, 'پرداخت کامل اشتراک سه ماهه');

-- Payments for Subscription 15 (Customer 7 - Pending Semi-annual)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (15, 2550000.00, NULL, CURRENT_DATE, 'PENDING', NULL, 'TXN-015-001', 0.00, 'در انتظار پرداخت');

-- Payments for Subscription 16 (Customer 8 - Overdue Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (16, 500000.00, DATEADD('MONTH', -3, CURRENT_TIMESTAMP), DATEADD('MONTH', -3, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-016-001', 0.00, 'پرداخت اولیه');

INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (16, 500000.00, DATEADD('MONTH', -2, CURRENT_TIMESTAMP), DATEADD('MONTH', -2, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-016-002', 0.00, 'پرداخت ماهانه');

INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (16, 500000.00, NULL, DATEADD('DAY', -8, CURRENT_DATE), 'PENDING', NULL, 'TXN-016-003', 80000.00, 'پرداخت معوقه با جریمه دیرکرد');

-- Payments for Subscription 17 (Customer 8 - Active Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (17, 500000.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), DATEADD('MONTH', -1, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-017-001', 0.00, 'پرداخت ماهانه');

-- Payments for Subscription 18 (Customer 8 - Cancelled Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (18, 1350000.00, DATEADD('MONTH', -5, CURRENT_TIMESTAMP), DATEADD('MONTH', -5, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-018-001', 0.00, 'پرداخت قبل از لغو');

-- Payments for Subscription 19 (Customer 9 - Active Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (19, 1350000.00, DATEADD('MONTH', -2, CURRENT_TIMESTAMP), DATEADD('MONTH', -2, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-019-001', 0.00, 'پرداخت کامل اشتراک سه ماهه');

-- Payments for Subscription 20 (Customer 9 - Pending Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (20, 500000.00, NULL, CURRENT_DATE, 'PENDING', NULL, 'TXN-020-001', 0.00, 'در انتظار پرداخت');

-- Payments for Subscription 21 (Customer 10 - Active Monthly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (21, 500000.00, DATEADD('MONTH', -1, CURRENT_TIMESTAMP), DATEADD('MONTH', -1, CURRENT_DATE), 'COMPLETED', 'کارت بانکی', 'TXN-021-001', 0.00, 'پرداخت ماهانه');

-- Payments for Subscription 22 (Customer 10 - Overdue Quarterly)
INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (22, 1350000.00, DATEADD('MONTH', -4, CURRENT_TIMESTAMP), DATEADD('MONTH', -4, CURRENT_DATE), 'COMPLETED', 'نقدی', 'TXN-022-001', 0.00, 'پرداخت اولیه');

INSERT INTO payments (subscription_id, amount, payment_date, due_date, status, payment_method, transaction_id, late_fee, notes)
VALUES (22, 450000.00, NULL, DATEADD('DAY', -12, CURRENT_DATE), 'PENDING', NULL, 'TXN-022-002', 120000.00, 'پرداخت معوقه با جریمه دیرکرد');
