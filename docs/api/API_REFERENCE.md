# API Reference

## Overview
RegularReception REST API - سیستم مدیریت اشتراک و پرداخت

این API یک سیستم جامع برای مدیریت مشتریان، اشتراک‌ها و پرداخت‌ها ارائه می‌دهد.

## Base URL
```
http://localhost:8081/api
```

## Authentication
تمام endpoint‌ها نیاز به احراز هویت JWT دارند.

### دریافت Token
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### استفاده از Token
```http
Authorization: Bearer {token}
```

## Endpoints

### 🏢 Business Management

#### Get Business Info
```http
GET /api/businesses/{id}
```

### 👥 Customer Management

#### List Customers
```http
GET /api/customers?page=0&size=20&sort=id,desc
```

#### Create Customer
```http
POST /api/customers
Content-Type: application/json

{
  "name": "علی احمدی",
  "phoneNumber": "09123456789",
  "email": "ali@example.com",
  "businessId": 1
}
```

#### Update Customer
```http
PUT /api/customers/{id}
```

#### Delete Customer
```http
DELETE /api/customers/{id}
```

### 📅 Subscription Management

#### List Subscriptions
```http
GET /api/subscriptions?status=ACTIVE
```

#### Create Subscription
```http
POST /api/subscriptions
Content-Type: application/json

{
  "customerId": 1,
  "paymentPlanId": 1,
  "startDate": "2025-01-01"
}
```

#### Get Subscription Details
```http
GET /api/subscriptions/{id}
```

#### Update Subscription Status
```http
PATCH /api/subscriptions/{id}/status
Content-Type: application/json

{
  "status": "SUSPENDED"
}
```

### 💳 Payment Management

#### List Payments
```http
GET /api/payments?subscriptionId=1
```

#### Create Payment
```http
POST /api/payments
Content-Type: application/json

{
  "subscriptionId": 1,
  "amount": 500000,
  "method": "ONLINE"
}
```

#### Process Payment
```http
POST /api/payments/{id}/process
```

### 💰 Payment Plan Management

#### List Payment Plans
```http
GET /api/payment-plans
```

#### Create Payment Plan
```http
POST /api/payment-plans
Content-Type: application/json

{
  "name": "اشتراک ماهانه",
  "amount": 500000,
  "durationDays": 30,
  "businessId": 1
}
```

## Status Codes

| Code | Description |
|------|-------------|
| 200  | Success |
| 201  | Created |
| 400  | Bad Request |
| 401  | Unauthorized |
| 403  | Forbidden |
| 404  | Not Found |
| 500  | Internal Server Error |

## Error Response Format

```json
{
  "timestamp": "2025-12-24T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data",
  "path": "/api/customers"
}
```

## Swagger UI

برای مستندات کامل و تعاملی، از Swagger UI استفاده کنید:

```
http://localhost:8081/swagger-ui.html
```

## OpenAPI Specification

```
http://localhost:8081/api-docs
```

## Rate Limiting

- **Default**: 100 requests per minute
- **Authenticated**: 1000 requests per minute

## Pagination

تمام endpoint‌های لیست از pagination پشتیبانی می‌کنند:

```
?page=0&size=20&sort=id,desc
```

**Response Format:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5
}
```

## Filtering

از query parameters برای فیلتر کردن استفاده کنید:

```
GET /api/customers?name=علی&phoneNumber=0912
GET /api/subscriptions?status=ACTIVE&businessId=1
```

## Sorting

```
?sort=name,asc
?sort=createdAt,desc
?sort=amount,desc&sort=date,asc
```

## Examples

### ایجاد یک مشتری و اشتراک کامل

```bash
# 1. Login
TOKEN=$(curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

# 2. Create Customer
CUSTOMER_ID=$(curl -X POST http://localhost:8081/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "علی احمدی",
    "phoneNumber": "09123456789",
    "email": "ali@example.com",
    "businessId": 1
  }' | jq -r '.id')

# 3. Create Subscription
SUBSCRIPTION_ID=$(curl -X POST http://localhost:8081/api/subscriptions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": $CUSTOMER_ID,
    \"paymentPlanId\": 1,
    \"startDate\": \"2025-01-01\"
  }" | jq -r '.id')

# 4. Create Payment
curl -X POST http://localhost:8081/api/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"subscriptionId\": $SUBSCRIPTION_ID,
    \"amount\": 500000,
    \"method\": \"ONLINE\"
  }"
```

## Support

برای سوالات بیشتر، به [README](../../README.md) مراجعه کنید.
