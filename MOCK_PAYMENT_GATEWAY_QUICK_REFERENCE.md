# Mock Payment Gateway - Quick Reference

## 🚀 Quick Start

### 1. Initiate Payment
```java
PaymentInitRequest request = PaymentInitRequest.builder()
    .subscriptionId(1L)
    .amount(new BigDecimal("500000"))
    .description("پرداخت اشتراک ماهانه")
    .build();

PaymentInitResponse response = paymentService.initiatePayment(request);
// Returns: transaction ID and payment URL
```

### 2. User Completes Payment
User visits the payment URL and clicks "Success" or "Cancel"

### 3. System Verifies Payment
```java
PaymentVerifyResponse response = paymentService.verifyMockPayment(transactionId, true);
// Updates payment status and subscription details
```

## 📡 REST API Quick Reference

### Initiate Payment
```bash
POST /api/payments/initiate
Content-Type: application/json

{
  "subscription_id": 1,
  "amount": 500000,
  "description": "پرداخت اشتراک"
}
```

### Verify Payment
```bash
POST /api/payments/verify-mock?transactionId=TXN123&success=true
```

### Get Payment History
```bash
GET /api/payments/history?businessId=1&startDate=2024-01-01&status=SUCCESS
```

## 💾 Database Schema

```sql
ALTER TABLE payments ADD COLUMN description TEXT;
ALTER TABLE payments ADD COLUMN created_at DATETIME;
ALTER TABLE payments ADD COLUMN paid_at DATETIME;

-- PaymentStatus enum now includes: PENDING, SUCCESS, FAILED, COMPLETED, REFUNDED
```

## 🔄 Payment Status Flow

```
PENDING → SUCCESS (payment completed)
        → FAILED  (payment cancelled)
```

**Note:** Once SUCCESS or FAILED, status cannot be changed.

## 🎯 Transaction ID Format

```
TXN{timestamp}{random}
Example: TXN17034567891234
```

## 📝 Key Methods

### PaymentService

```java
// Initiate payment
PaymentInitResponse initiatePayment(PaymentInitRequest request)

// Verify payment
PaymentVerifyResponse verifyMockPayment(String transactionId, boolean success)

// Get history
List<PaymentResponse> getPaymentHistory(Long businessId, LocalDate startDate, 
                                       LocalDate endDate, PaymentStatus status)
```

## 🛠️ Testing Commands

### Successful Payment Flow
```bash
# Step 1: Initiate
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{"subscription_id": 1, "amount": 500000, "description": "Test"}'

# Step 2: Verify as success
curl -X POST "http://localhost:8080/api/payments/verify-mock?transactionId=TXN123&success=true"

# Step 3: Check history
curl "http://localhost:8080/api/payments/history?businessId=1&status=SUCCESS"
```

## 🎨 Payment Gateway URL

```
http://localhost:8080/payment-gateway.html?transactionId=TXN123&amount=500000
```

## ⚠️ Common Issues

### Transaction not found
✅ Ensure payment was initiated first  
✅ Check transaction ID is correct  

### Payment already processed
✅ Expected behavior - payments can only be verified once  

### Subscription not updating
✅ Check subscription status (must not be CANCELLED)  
✅ Verify payment verification returned success  

## 📊 What Gets Updated

When payment is SUCCESS:
- ✅ Payment status → SUCCESS
- ✅ Payment paid_at → Current timestamp
- ✅ Subscription last_payment_date → Current date
- ✅ Subscription next_payment_date → Calculated based on plan
- ✅ Subscription status → ACTIVE (if was OVERDUE/PENDING)

## 🔒 Security Notes

- All operations are @Transactional
- Transaction IDs are unique
- Duplicate processing prevented
- All operations logged
- Subscription validation performed

## 📚 Documentation Files

- `MOCK_PAYMENT_GATEWAY.md` - Complete documentation
- `MOCK_PAYMENT_GATEWAY_SUMMARY.md` - Implementation summary
- `payment-gateway.html` - Mock gateway UI

## 💡 Tips

1. Always check application logs for detailed error messages
2. Transaction IDs are case-sensitive
3. Persian messages are returned for user-facing errors
4. Payment status transitions are one-way (no reversals in mock)
5. Use Swagger UI at `/swagger-ui.html` to test endpoints

## 🎯 Production Checklist

Before going to production:
- [ ] Replace with real payment gateway integration
- [ ] Add authentication/authorization
- [ ] Implement webhook handling
- [ ] Add payment reconciliation
- [ ] Enable fraud detection
- [ ] Set up monitoring and alerts
- [ ] Add email/SMS notifications
- [ ] Implement refund functionality

---

**Quick Help:** See `MOCK_PAYMENT_GATEWAY.md` for detailed documentation
