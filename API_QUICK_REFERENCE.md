# API Quick Reference Guide

## Base URL
```
http://localhost:8080/api
```

## Business Management (`/businesses`)

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/businesses` | Create new business | 201 |
| GET | `/businesses/{id}` | Get business details | 200 |
| PUT | `/businesses/{id}` | Update business | 200 |
| DELETE | `/businesses/{id}` | Delete business | 204 |
| GET | `/businesses` | List all businesses (paginated) | 200 |
| GET | `/businesses/{id}/dashboard` | Get dashboard statistics | 200 |

**Query Parameters (List):**
- `page` (default: 0)
- `size` (default: 10)
- `sortBy` (default: id)
- `sortDir` (default: asc)

---

## Customer Management (`/customers`)

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/customers/business/{businessId}` | Add customer to business | 201 |
| GET | `/customers/{id}` | Get customer details | 200 |
| PUT | `/customers/{id}` | Update customer | 200 |
| DELETE | `/customers/{id}` | Delete customer | 204 |
| GET | `/customers/business/{businessId}` | List customers (paginated) | 200 |
| GET | `/customers/search` | Search customers | 200 |
| GET | `/customers/{id}/subscriptions` | Get customer subscriptions | 200 |

**Query Parameters (Search):**
- `businessId` (required)
- `keyword` (required)

**Query Parameters (List):**
- `page`, `size`, `sortBy`, `sortDir`

---

## Payment Plan Management (`/payment-plans`)

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/payment-plans/business/{businessId}` | Create payment plan | 201 |
| GET | `/payment-plans/{id}` | Get payment plan | 200 |
| PUT | `/payment-plans/{id}` | Update payment plan | 200 |
| DELETE | `/payment-plans/{id}` | Delete payment plan | 204 |
| GET | `/payment-plans/business/{businessId}` | List payment plans | 200 |

---

## Subscription Management (`/subscriptions`)

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/subscriptions` | Create subscription | 201 |
| GET | `/subscriptions/{id}` | Get subscription details | 200 |
| PUT | `/subscriptions/{id}/renew` | Renew subscription | 200 |
| PUT | `/subscriptions/{id}/cancel` | Cancel subscription | 200 |
| GET | `/subscriptions/customer/{customerId}` | Get customer subscriptions | 200 |
| GET | `/subscriptions/status/{status}` | Get subscriptions by status | 200 |

**Status Values:**
- `ACTIVE`
- `OVERDUE`
- `EXPIRED`
- `CANCELLED`

---

## Payment Management (`/payments`)

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/payments` | Process payment | 201 |
| GET | `/payments/{id}` | Get payment details | 200 |
| GET | `/payments/subscription/{subscriptionId}` | Get subscription payments | 200 |
| GET | `/payments/customer/{customerId}` | Get payment history (paginated) | 200 |
| POST | `/payments/verify` | Verify payment | 200 |
| GET | `/payments/{subscriptionId}/link` | Generate payment link | 200 |
| GET | `/payments/customer/{customerId}/pending` | Get pending payments | 200 |

**Query Parameters (Verify):**
- `transactionId` (required)

**Query Parameters (History):**
- `page`, `size`, `sortBy` (default: paymentDate), `sortDir` (default: desc)

---

## Request Examples

### Create Business
```json
POST /api/businesses
{
  "businessName": "Tech Solutions Ltd",
  "ownerName": "علی احمدی",
  "contactEmail": "info@techsolutions.com",
  "contactPhone": "09123456789",
  "description": "IT consulting services"
}
```

### Create Customer
```json
POST /api/customers/business/1
{
  "firstName": "محمد",
  "lastName": "رضایی",
  "phoneNumber": "09123456789",
  "email": "mohammad@example.com",
  "customerType": "INDIVIDUAL",
  "joinDate": "2025-01-01"
}
```

### Create Payment Plan
```json
POST /api/payment-plans/business/1
{
  "planName": "Monthly Standard",
  "periodType": "MONTHLY",
  "periodCount": 1,
  "baseAmount": 100000,
  "discountPercentage": 10,
  "lateFeePerDay": 5000,
  "gracePeriodDays": 3
}
```

### Create Subscription
```json
POST /api/subscriptions
{
  "customerId": 1,
  "paymentPlanId": 1,
  "startDate": "2025-01-01",
  "numberOfPeriods": 3
}
```

### Process Payment
```json
POST /api/payments
{
  "subscriptionId": 1,
  "amount": 100000,
  "paymentMethod": "CASH",
  "notes": "Payment received in full"
}
```

---

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "field_errors": {
    "fieldName": "Error message"
  },
  "timestamp": "2025-12-22T10:30:00"
}
```

### Paginated Response
```json
{
  "success": true,
  "message": "Data retrieved successfully",
  "data": {
    "content": [...],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "last": false
  }
}
```

---

## HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT operations |
| 201 | Created | Successful POST operations |
| 204 | No Content | Successful DELETE operations |
| 400 | Bad Request | Validation errors, invalid data |
| 404 | Not Found | Resource doesn't exist |
| 500 | Internal Server Error | Unexpected errors |

---

## Common Query Parameters

### Pagination
- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default: 10)
- `sortBy`: Field to sort by (default varies by endpoint)
- `sortDir`: Sort direction (`asc` or `desc`)

**Example:**
```
GET /api/businesses?page=0&size=20&sortBy=businessName&sortDir=asc
```

---

## CORS Configuration

**Allowed Origins:**
- `http://localhost:*`
- `http://127.0.0.1:*`
- `https://*.example.com` (configure for production)

**Allowed Methods:**
- GET, POST, PUT, DELETE, PATCH, OPTIONS

**Credentials:**
- Enabled (cookies, authorization headers)

---

## Swagger Documentation

Access interactive API documentation:
```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:8080/v3/api-docs
```

---

## Testing with cURL

### Get Business
```bash
curl http://localhost:8080/api/businesses/1
```

### Create Customer
```bash
curl -X POST http://localhost:8080/api/customers/business/1 \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","phoneNumber":"09123456789","customerType":"INDIVIDUAL"}'
```

### Search Customers
```bash
curl "http://localhost:8080/api/customers/search?businessId=1&keyword=John"
```

### Get Dashboard
```bash
curl http://localhost:8080/api/businesses/1/dashboard
```

### Process Payment
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"subscriptionId":1,"amount":100000,"paymentMethod":"CASH"}'
```

---

## Validation Rules

### Business
- `businessName`: Required, max 100 characters
- `ownerName`: Required, max 100 characters
- `contactEmail`: Valid email format
- `contactPhone`: Iranian format (09xxxxxxxxx)

### Customer
- `firstName`: Required, max 50 characters
- `lastName`: Required, max 50 characters
- `phoneNumber`: Required, Iranian format
- `email`: Valid email format
- `customerType`: INDIVIDUAL or BUSINESS

### Payment Plan
- `planName`: Required
- `periodType`: DAILY, WEEKLY, MONTHLY, QUARTERLY, SEMI_ANNUAL, YEARLY
- `periodCount`: Positive integer
- `baseAmount`: Positive number

### Payment
- `subscriptionId`: Required
- `amount`: Required, positive number
- `paymentMethod`: CASH, BANK_TRANSFER, CREDIT_CARD, ONLINE

---

## Tips

1. **Use Swagger UI** for interactive testing and documentation
2. **Enable logging** to see request/response details
3. **Test pagination** with different page sizes
4. **Check validation** by submitting invalid data
5. **Monitor dashboard** for real-time business metrics
6. **Use search** for quick customer lookup
7. **Track payments** through subscription history

---

## Environment Variables

Configure in `application.yml`:
```yaml
server:
  port: 8080

spring:
  application:
    name: daryaft-core
```

---

## Support

For issues or questions:
- Email: support@daryaftmanazam.com
- Documentation: https://docs.daryaftmanazam.com
- Swagger UI: http://localhost:8080/swagger-ui.html
