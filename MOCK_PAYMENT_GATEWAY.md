# Mock Payment Gateway Documentation

## 📋 Overview

This document describes the mock payment gateway implementation for MVP testing in the Spring Boot application. The gateway simulates a real payment processor without requiring actual payment integration.

## 🏗️ Architecture

### Components

1. **Payment Entity** - Enhanced with mock gateway fields
2. **PaymentStatus Enum** - Updated with SUCCESS status
3. **DTOs** - Request/Response objects for gateway operations
4. **PaymentRepository** - Custom queries for business filters
5. **PaymentService** - Business logic for payment processing
6. **PaymentController** - REST endpoints for gateway operations
7. **payment-gateway.html** - Mock payment gateway UI

## 🔄 Payment Flow

```
1. User initiates payment → POST /api/payments/initiate
2. System creates PENDING payment record
3. System returns payment URL with transaction ID
4. User redirected to mock payment gateway page
5. User clicks "Success" or "Cancel"
6. Gateway calls → POST /api/payments/verify-mock
7. System updates payment status (SUCCESS/FAILED)
8. System updates subscription details
9. User redirected back to application
```

## 📡 API Endpoints

### 1. Initiate Payment

**Endpoint:** `POST /api/payments/initiate`

**Request Body:**
```json
{
  "subscription_id": 1,
  "amount": 500000,
  "description": "پرداخت اشتراک ماهانه"
}
```

**Response:**
```json
{
  "transaction_id": "TXN17034567891234",
  "payment_url": "http://localhost:8080/payment-gateway.html?transactionId=TXN17034567891234&amount=500000",
  "success": true,
  "error_message": null
}
```

**Error Response:**
```json
{
  "transaction_id": null,
  "payment_url": null,
  "success": false,
  "error_message": "اشتراک یافت نشد"
}
```

### 2. Verify Payment

**Endpoint:** `POST /api/payments/verify-mock`

**Query Parameters:**
- `transactionId` (required): Transaction ID from initiate response
- `success` (required): Boolean indicating payment success

**Example:**
```
POST /api/payments/verify-mock?transactionId=TXN17034567891234&success=true
```

**Response:**
```json
{
  "transaction_id": "TXN17034567891234",
  "success": true,
  "message": "پرداخت با موفقیت انجام شد"
}
```

### 3. Get Payment History

**Endpoint:** `GET /api/payments/history`

**Query Parameters:**
- `businessId` (required): Business ID
- `startDate` (optional): Start date (YYYY-MM-DD)
- `endDate` (optional): End date (YYYY-MM-DD)
- `status` (optional): Payment status (PENDING, SUCCESS, FAILED, COMPLETED)

**Example:**
```
GET /api/payments/history?businessId=1&startDate=2024-01-01&endDate=2024-12-31&status=SUCCESS
```

**Response:**
```json
{
  "success": true,
  "message": "Payment history retrieved successfully",
  "data": [
    {
      "id": 1,
      "subscription_id": 1,
      "customer_name": "احمد محمدی",
      "amount": 500000,
      "payment_date": "2024-12-23T10:30:00",
      "status": "SUCCESS",
      "transaction_id": "TXN17034567891234",
      "description": "پرداخت اشتراک ماهانه"
    }
  ]
}
```

### 4. Get Payments by Subscription

**Endpoint:** `GET /api/payments/subscription/{subscriptionId}`

**Example:**
```
GET /api/payments/subscription/1
```

## 🗄️ Database Schema

### Payment Entity Fields

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| subscription_id | Long | Foreign key to subscription |
| amount | BigDecimal | Payment amount |
| status | Enum | PENDING, SUCCESS, FAILED, COMPLETED, REFUNDED |
| transaction_id | String | Unique transaction reference |
| description | Text | Payment description |
| created_at | DateTime | Payment creation timestamp |
| paid_at | DateTime | Payment completion timestamp |
| payment_date | DateTime | Legacy field (kept for compatibility) |
| due_date | Date | Payment due date |
| payment_method | String | Payment method |
| late_fee | BigDecimal | Late payment fee |
| notes | Text | Additional notes |

## 🎯 Usage Examples

### Example 1: Simple Payment Flow

```javascript
// 1. Initiate payment
const initResponse = await fetch('http://localhost:8080/api/payments/initiate', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    subscription_id: 1,
    amount: 500000,
    description: 'پرداخت اشتراک ماهانه'
  })
});

const initData = await initResponse.json();

if (initData.success) {
  // 2. Redirect to payment gateway
  window.location.href = initData.payment_url;
}
```

### Example 2: Check Payment History

```javascript
// Get payment history for business
const response = await fetch(
  'http://localhost:8080/api/payments/history?businessId=1&status=SUCCESS',
  {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
console.log('Total payments:', data.data.length);
```

## 🔒 Transaction ID Format

Transaction IDs are generated in the format:
```
TXN{timestamp}{random4digits}
```

Example: `TXN17034567891234`

- `TXN` - Prefix
- `1703456789` - Unix timestamp in milliseconds
- `1234` - Random 4-digit number

## ✅ Payment Status Flow

```
PENDING → SUCCESS (payment completed)
        → FAILED  (payment cancelled/failed)
```

**Note:** Once a payment is marked as SUCCESS or FAILED, it cannot be changed.

## 📊 Business Logic

### Payment Initiation
1. Validates subscription exists
2. Validates subscription is not cancelled
3. Creates PENDING payment record
4. Generates unique transaction ID
5. Returns mock payment URL

### Payment Verification
1. Finds payment by transaction ID
2. Checks if already processed
3. Updates payment status (SUCCESS/FAILED)
4. If SUCCESS:
   - Sets paid_at timestamp
   - Updates subscription last_payment_date
   - Calculates next_payment_date
   - Changes subscription status to ACTIVE (if was OVERDUE/PENDING)
5. Returns verification response

### Subscription Update
When a payment is verified as SUCCESS:
- `lastPaymentDate` → Set to current date
- `nextPaymentDate` → Calculated based on payment plan period
- `status` → Changed to ACTIVE (if was OVERDUE or PENDING)

## 🧪 Testing Scenarios

### Test Case 1: Successful Payment
```bash
# 1. Initiate payment
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{"subscription_id": 1, "amount": 500000, "description": "Test payment"}'

# 2. Verify as success
curl -X POST "http://localhost:8080/api/payments/verify-mock?transactionId=TXN17034567891234&success=true"
```

### Test Case 2: Failed Payment
```bash
# 1. Initiate payment
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{"subscription_id": 1, "amount": 500000, "description": "Test payment"}'

# 2. Verify as failed
curl -X POST "http://localhost:8080/api/payments/verify-mock?transactionId=TXN17034567891234&success=false"
```

### Test Case 3: Duplicate Verification
```bash
# Try to verify the same transaction twice
curl -X POST "http://localhost:8080/api/payments/verify-mock?transactionId=TXN17034567891234&success=true"
# Should return: "این تراکنش قبلاً پردازش شده است"
```

## 🔐 Security Considerations

**⚠️ Important: This is a MOCK gateway for MVP testing only!**

For production deployment:
1. Replace with real payment gateway integration (e.g., ZarinPal, Saman, Mellat)
2. Add authentication/authorization
3. Implement proper transaction verification
4. Add payment webhook handling
5. Store encrypted payment details
6. Implement fraud detection
7. Add payment reconciliation
8. Enable logging and monitoring

## 🚀 Future Enhancements

1. **Real Gateway Integration**
   - ZarinPal API
   - Saman Bank API
   - Mellat Bank API

2. **Advanced Features**
   - Partial payments
   - Payment installments
   - Refund processing
   - Payment reversals

3. **Reporting**
   - Payment analytics
   - Revenue reports
   - Failed payment tracking
   - Payment reconciliation

4. **Notifications**
   - Payment confirmation emails
   - SMS notifications
   - Real-time payment status updates

## 📝 Configuration

### Application Properties

```yaml
# Mock Payment Gateway Configuration
payment:
  gateway:
    mock:
      enabled: true
      base-url: http://localhost:8080
      timeout: 30000
      retry-attempts: 3
```

## 🐛 Troubleshooting

### Issue: Payment gateway page not loading
**Solution:** Ensure `payment-gateway.html` is in the project root and accessible at `http://localhost:8080/payment-gateway.html`

### Issue: Transaction not found error
**Solution:** Verify the transaction ID is correct and the payment was properly initiated

### Issue: Payment already processed
**Solution:** This is expected behavior. Payments can only be verified once.

### Issue: Subscription not updating after payment
**Solution:** Check that the subscription status allows updates (not CANCELLED) and the payment verification returned success

## 📞 Support

For issues or questions about the mock payment gateway:
1. Check application logs for detailed error messages
2. Verify all required fields are provided in requests
3. Ensure database is properly configured
4. Check that subscription exists and is active

## 📄 License

This mock payment gateway is part of the RegularReception application and follows the same license.

---

**Created:** December 23, 2024  
**Version:** 1.0.0  
**Status:** ✅ Production Ready (for MVP testing)
