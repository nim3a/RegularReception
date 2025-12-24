package com.daryaftmanazam.daryaftcore.config;

import com.daryaftmanazam.daryaftcore.model.*;
import com.daryaftmanazam.daryaftcore.model.enums.*;
import com.daryaftmanazam.daryaftcore.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Component to initialize sample data if database is empty.
 * This component runs after the application context is loaded.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final PaymentPlanRepository paymentPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    private final Random random = new Random();

    @PostConstruct
    @Transactional
    public void initializeData() {
        if (isDatabaseEmpty()) {
            log.info("Database is empty. Starting data initialization...");
            try {
                initializeSampleData();
                log.info("✅ Data initialization completed successfully!");
            } catch (Exception e) {
                log.error("❌ Error during data initialization: {}", e.getMessage(), e);
            }
        } else {
            log.info("Database already contains data. Skipping initialization.");
        }
    }

    private boolean isDatabaseEmpty() {
        long businessCount = businessRepository.count();
        log.info("Current business count in database: {}", businessCount);
        return businessCount == 0;
    }

    private void initializeSampleData() {
        log.info("📊 Creating businesses...");
        List<Business> businesses = createBusinesses();
        
        log.info("💳 Creating payment plans...");
        List<PaymentPlan> paymentPlans = createPaymentPlans(businesses);
        
        log.info("👥 Creating customers...");
        List<Customer> customers = createCustomers(businesses);
        
        log.info("📝 Creating subscriptions...");
        List<Subscription> subscriptions = createSubscriptions(customers, paymentPlans);
        
        log.info("💰 Creating payment records...");
        createPayments(subscriptions);
        
        logSummary(businesses, customers, subscriptions);
    }

    private List<Business> createBusinesses() {
        List<Business> businesses = new ArrayList<>();

        // Business 1: باشگاه بی رانرز
        Business gym = Business.builder()
                .businessName("باشگاه بی رانرز")
                .ownerName("ویدا مختاری")
                .contactEmail("info@berunners.ir")
                .contactPhone("09121234567")
                .description("باشگاه ورزشی و دو")
                .isActive(true)
                .build();
        businesses.add(businessRepository.save(gym));
        log.info("✓ Created business: باشگاه بی رانرز (Owner: ویدا مختاری)");

        // Business 2: مجتمع مسکونی پارسیان
        Business residential = Business.builder()
                .businessName("مجتمع مسکونی پارسیان")
                .ownerName("محمد رضایی")
                .contactEmail("manager@parsian.ir")
                .contactPhone("09131234567")
                .description("مجتمع مسکونی با امکانات رفاهی")
                .isActive(true)
                .build();
        businesses.add(businessRepository.save(residential));
        log.info("✓ Created business: مجتمع مسکونی پارسیان (Owner: محمد رضایی)");

        return businesses;
    }

    private List<PaymentPlan> createPaymentPlans(List<Business> businesses) {
        List<PaymentPlan> plans = new ArrayList<>();

        for (Business business : businesses) {
            // Monthly Plan - 500,000 Toman
            PaymentPlan monthly = PaymentPlan.builder()
                    .planName(business.getBusinessName().contains("باشگاه") ? "اشتراک ماهانه" : "شارژ ماهانه")
                    .periodType(PeriodType.MONTHLY)
                    .periodCount(1)
                    .baseAmount(new BigDecimal("500000.00"))
                    .discountPercentage(BigDecimal.ZERO)
                    .lateFeePerDay(new BigDecimal(business.getBusinessName().contains("باشگاه") ? "10000.00" : "15000.00"))
                    .gracePeriodDays(3)
                    .isActive(true)
                    .business(business)
                    .build();
            plans.add(paymentPlanRepository.save(monthly));

            // Quarterly Plan - 1,350,000 Toman (10% discount)
            PaymentPlan quarterly = PaymentPlan.builder()
                    .planName(business.getBusinessName().contains("باشگاه") ? "اشتراک سه ماهه" : "شارژ سه ماهه")
                    .periodType(PeriodType.QUARTERLY)
                    .periodCount(3)
                    .baseAmount(new BigDecimal("1500000.00"))
                    .discountPercentage(new BigDecimal("10.00"))
                    .lateFeePerDay(new BigDecimal(business.getBusinessName().contains("باشگاه") ? "10000.00" : "15000.00"))
                    .gracePeriodDays(5)
                    .isActive(true)
                    .business(business)
                    .build();
            plans.add(paymentPlanRepository.save(quarterly));

            // Semi-annual Plan - 2,550,000 Toman (15% discount)
            PaymentPlan semiAnnual = PaymentPlan.builder()
                    .planName(business.getBusinessName().contains("باشگاه") ? "اشتراک شش ماهه" : "شارژ شش ماهه")
                    .periodType(PeriodType.SEMI_ANNUAL)
                    .periodCount(6)
                    .baseAmount(new BigDecimal("3000000.00"))
                    .discountPercentage(new BigDecimal("15.00"))
                    .lateFeePerDay(new BigDecimal(business.getBusinessName().contains("باشگاه") ? "10000.00" : "15000.00"))
                    .gracePeriodDays(7)
                    .isActive(true)
                    .business(business)
                    .build();
            plans.add(paymentPlanRepository.save(semiAnnual));

            log.info("✓ Created 3 payment plans for: {}", business.getBusinessName());
        }

        return plans;
    }

    private List<Customer> createCustomers(List<Business> businesses) {
        List<Customer> customers = new ArrayList<>();

        // Persian names for customers
        String[][] gymCustomers = {
                {"علی", "احمدی", "09121111111", "ali.ahmadi@email.com"},
                {"زهرا", "محمدی", "09122222222", "zahra.mohammadi@email.com"},
                {"حسین", "کریمی", "09123333333", "hossein.karimi@email.com"},
                {"فاطمه", "رضایی", "09124444444", "fatemeh.rezaei@email.com"},
                {"مهدی", "نوری", "09125555555", "mahdi.noori@email.com"}
        };

        String[][] residentialCustomers = {
                {"سارا", "حسینی", "09131111111", "sara.hosseini@email.com"},
                {"رضا", "مرادی", "09132222222", "reza.moradi@email.com"},
                {"مریم", "صادقی", "09133333333", "maryam.sadeghi@email.com"},
                {"امیر", "جعفری", "09134444444", "amir.jafari@email.com"},
                {"لیلا", "باقری", "09135555555", "leila.bagheri@email.com"}
        };

        // Create customers for first business (Gym)
        for (int i = 0; i < 5; i++) {
            Customer customer = Customer.builder()
                    .firstName(gymCustomers[i][0])
                    .lastName(gymCustomers[i][1])
                    .phoneNumber(gymCustomers[i][2])
                    .email(gymCustomers[i][3])
                    .customerType(getCustomerType(i))
                    .isActive(true)
                    .joinDate(LocalDate.now().minusMonths(5 - i))
                    .business(businesses.get(0))
                    .build();
            customers.add(customerRepository.save(customer));
        }
        log.info("✓ Created 5 customers for: باشگاه بی رانرز");

        // Create customers for second business (Residential)
        for (int i = 0; i < 5; i++) {
            Customer customer = Customer.builder()
                    .firstName(residentialCustomers[i][0])
                    .lastName(residentialCustomers[i][1])
                    .phoneNumber(residentialCustomers[i][2])
                    .email(residentialCustomers[i][3])
                    .customerType(getCustomerType(i))
                    .isActive(true)
                    .joinDate(LocalDate.now().minusMonths(5 - i))
                    .business(businesses.get(1))
                    .build();
            customers.add(customerRepository.save(customer));
        }
        log.info("✓ Created 5 customers for: مجتمع مسکونی پارسیان");

        return customers;
    }

    private CustomerType getCustomerType(int index) {
        return switch (index % 5) {
            case 1 -> CustomerType.VIP;
            case 3 -> CustomerType.NEW;
            default -> CustomerType.REGULAR;
        };
    }

    private List<Subscription> createSubscriptions(List<Customer> customers, List<PaymentPlan> plans) {
        List<Subscription> subscriptions = new ArrayList<>();

        for (Customer customer : customers) {
            // Get payment plans for this customer's business
            List<PaymentPlan> businessPlans = plans.stream()
                    .filter(p -> p.getBusiness().getId().equals(customer.getBusiness().getId()))
                    .toList();

            // Create 2-3 subscriptions per customer
            int subscriptionCount = 2 + random.nextInt(2); // 2 or 3 subscriptions
            
            for (int i = 0; i < subscriptionCount; i++) {
                PaymentPlan plan = businessPlans.get(random.nextInt(businessPlans.size()));
                SubscriptionStatus status = getSubscriptionStatus(i);
                
                LocalDate startDate = getStartDate(i);
                LocalDate endDate = getEndDate(startDate, plan.getPeriodType(), status);
                LocalDate nextPaymentDate = getNextPaymentDate(status, endDate);
                
                BigDecimal totalAmount = calculateTotalAmount(plan);
                BigDecimal discountApplied = calculateDiscount(plan);

                Subscription subscription = Subscription.builder()
                        .customer(customer)
                        .paymentPlan(plan)
                        .startDate(startDate)
                        .endDate(endDate)
                        .status(status)
                        .totalAmount(totalAmount)
                        .discountApplied(discountApplied)
                        .nextPaymentDate(nextPaymentDate)
                        .build();
                
                subscriptions.add(subscriptionRepository.save(subscription));
            }
        }

        log.info("✓ Created {} subscriptions across all customers", subscriptions.size());
        return subscriptions;
    }

    private SubscriptionStatus getSubscriptionStatus(int index) {
        return switch (index % 3) {
            case 0 -> SubscriptionStatus.ACTIVE;
            case 1 -> random.nextBoolean() ? SubscriptionStatus.OVERDUE : SubscriptionStatus.PENDING;
            default -> random.nextBoolean() ? SubscriptionStatus.EXPIRED : SubscriptionStatus.CANCELLED;
        };
    }

    private LocalDate getStartDate(int index) {
        int monthsAgo = 1 + random.nextInt(5); // 1-5 months ago
        return LocalDate.now().minusMonths(monthsAgo);
    }

    private LocalDate getEndDate(LocalDate startDate, PeriodType periodType, SubscriptionStatus status) {
        int months = switch (periodType) {
            case DAILY -> 0;
            case WEEKLY -> 0;
            case MONTHLY -> 1;
            case QUARTERLY -> 3;
            case SEMI_ANNUAL -> 6;
            case YEARLY -> 12;
        };

        LocalDate calculatedEndDate = startDate.plusMonths(months);

        // Adjust based on status
        return switch (status) {
            case EXPIRED, CANCELLED -> calculatedEndDate.minusMonths(1);
            case OVERDUE -> LocalDate.now().minusDays(random.nextInt(15) + 1);
            default -> calculatedEndDate;
        };
    }

    private LocalDate getNextPaymentDate(SubscriptionStatus status, LocalDate endDate) {
        return switch (status) {
            case ACTIVE -> LocalDate.now().plusDays(5 + random.nextInt(25));
            case OVERDUE -> LocalDate.now().minusDays(5 + random.nextInt(15));
            case PENDING -> LocalDate.now();
            case EXPIRED, CANCELLED -> endDate;
        };
    }

    private BigDecimal calculateTotalAmount(PaymentPlan plan) {
        BigDecimal baseAmount = plan.getBaseAmount();
        BigDecimal discount = plan.getDiscountPercentage();
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = baseAmount.multiply(discount).divide(new BigDecimal("100"));
            return baseAmount.subtract(discountAmount);
        }
        return baseAmount;
    }

    private BigDecimal calculateDiscount(PaymentPlan plan) {
        BigDecimal baseAmount = plan.getBaseAmount();
        BigDecimal discount = plan.getDiscountPercentage();
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            return baseAmount.multiply(discount).divide(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    private void createPayments(List<Subscription> subscriptions) {
        int paymentCount = 0;

        for (Subscription subscription : subscriptions) {
            SubscriptionStatus status = subscription.getStatus();
            
            // Create payments based on subscription status
            switch (status) {
                case ACTIVE -> {
                    // 1-2 completed payments
                    int completedPayments = 1 + random.nextInt(2);
                    for (int i = 0; i < completedPayments; i++) {
                        createCompletedPayment(subscription, i);
                        paymentCount++;
                    }
                }
                case OVERDUE -> {
                    // 1 completed payment + 1 pending with late fee
                    createCompletedPayment(subscription, 0);
                    createOverduePayment(subscription);
                    paymentCount += 2;
                }
                case PENDING -> {
                    // 1 pending payment
                    createPendingPayment(subscription);
                    paymentCount++;
                }
                case EXPIRED, CANCELLED -> {
                    // 1 completed payment from the past
                    createCompletedPayment(subscription, 0);
                    paymentCount++;
                }
            }
        }

        log.info("✓ Created {} payment records", paymentCount);
    }

    private void createCompletedPayment(Subscription subscription, int index) {
        LocalDateTime paymentDate = subscription.getStartDate().plusMonths(index).atStartOfDay();
        
        Payment payment = Payment.builder()
                .subscription(subscription)
                .amount(subscription.getTotalAmount())
                .paymentDate(paymentDate)
                .dueDate(subscription.getStartDate().plusMonths(index))
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(random.nextBoolean() ? "کارت بانکی" : "نقدی")
                .transactionId(generateTransactionId(subscription.getId(), index))
                .lateFee(BigDecimal.ZERO)
                .notes(index == 0 ? "پرداخت اولیه" : "پرداخت ماهانه")
                .build();
        
        paymentRepository.save(payment);
    }

    private void createOverduePayment(Subscription subscription) {
        int daysOverdue = 5 + random.nextInt(15);
        BigDecimal lateFee = subscription.getPaymentPlan().getLateFeePerDay()
                .multiply(new BigDecimal(daysOverdue));
        
        Payment payment = Payment.builder()
                .subscription(subscription)
                .amount(subscription.getTotalAmount())
                .paymentDate(null)
                .dueDate(LocalDate.now().minusDays(daysOverdue))
                .status(PaymentStatus.PENDING)
                .paymentMethod(null)
                .transactionId(generateTransactionId(subscription.getId(), 99))
                .lateFee(lateFee)
                .notes("پرداخت معوقه با جریمه دیرکرد")
                .build();
        
        paymentRepository.save(payment);
    }

    private void createPendingPayment(Subscription subscription) {
        Payment payment = Payment.builder()
                .subscription(subscription)
                .amount(subscription.getTotalAmount())
                .paymentDate(null)
                .dueDate(LocalDate.now())
                .status(PaymentStatus.PENDING)
                .paymentMethod(null)
                .transactionId(generateTransactionId(subscription.getId(), 0))
                .lateFee(BigDecimal.ZERO)
                .notes("در انتظار پرداخت")
                .build();
        
        paymentRepository.save(payment);
    }

    private String generateTransactionId(Long subscriptionId, int index) {
        return String.format("TXN-%03d-%03d", subscriptionId, index + 1);
    }

    private void logSummary(List<Business> businesses, List<Customer> customers, List<Subscription> subscriptions) {
        log.info("═══════════════════════════════════════════════════════");
        log.info("📊 DATA INITIALIZATION SUMMARY");
        log.info("═══════════════════════════════════════════════════════");
        log.info("🏢 Businesses created: {}", businesses.size());
        log.info("👥 Customers created: {}", customers.size());
        log.info("💳 Payment plans created: {}", paymentPlanRepository.count());
        log.info("📝 Subscriptions created: {}", subscriptions.size());
        log.info("💰 Payments created: {}", paymentRepository.count());
        log.info("───────────────────────────────────────────────────────");
        
        for (Business business : businesses) {
            long customerCount = customerRepository.countByBusinessId(business.getId());
            long planCount = paymentPlanRepository.countByBusinessId(business.getId());
            log.info("📍 {}: {} customers, {} payment plans", 
                    business.getBusinessName(), customerCount, planCount);
        }
        
        log.info("───────────────────────────────────────────────────────");
        log.info("📊 Subscription Status Breakdown:");
        log.info("   ✓ ACTIVE: {}", subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE));
        log.info("   ⏳ PENDING: {}", subscriptionRepository.countByStatus(SubscriptionStatus.PENDING));
        log.info("   ⚠ OVERDUE: {}", subscriptionRepository.countByStatus(SubscriptionStatus.OVERDUE));
        log.info("   ✗ EXPIRED: {}", subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED));
        log.info("   ✗ CANCELLED: {}", subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED));
        log.info("═══════════════════════════════════════════════════════");
    }
}
