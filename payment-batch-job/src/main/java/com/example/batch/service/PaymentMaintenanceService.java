package com.example.batch.service;

import com.example.batch.entity.Payment;
import com.example.batch.entity.PaymentStatus;
import com.example.batch.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service that performs payment maintenance tasks
 */
@Service
public class PaymentMaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMaintenanceService.class);

    private final PaymentRepository paymentRepository;

    @Value("${batch.payment.stale-hours:24}")
    private int staleHours;

    @Autowired
    public PaymentMaintenanceService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Expire stale payments that have been pending for too long
     * @return number of payments expired
     */
    @Transactional
    public int expireStalePayments() {
        String batchId = UUID.randomUUID().toString();
        MDC.put("batchId", batchId);
        MDC.put("operation", "expireStalePayments");

        logger.info("Starting stale payment expiration process. Looking for payments older than {} hours", staleHours);

        try {
            LocalDateTime expiryDate = LocalDateTime.now().minusHours(staleHours);
            List<Payment> stalePayments = paymentRepository.findStalePayments(expiryDate);

            logger.info("Found {} stale payments to expire", stalePayments.size());

            int expiredCount = 0;
            for (Payment payment : stalePayments) {
                MDC.put("paymentId", payment.getId());

                try {
                    logger.info("Expiring payment {} created at {}", payment.getId(), payment.getCreatedAt());

                    payment.setStatus(PaymentStatus.EXPIRED);
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    expiredCount++;
                    logger.info("Successfully expired payment {}", payment.getId());

                } catch (Exception e) {
                    logger.error("Failed to expire payment {}", payment.getId(), e);
                } finally {
                    MDC.remove("paymentId");
                }
            }

            logger.info("Stale payment expiration completed. Expired {} out of {} payments",
                    expiredCount, stalePayments.size());

            return expiredCount;

        } finally {
            MDC.clear();
        }
    }

    /**
     * Additional maintenance task: Clean up old completed payments
     * This is just an example of another batch operation
     */
    @Transactional
    public int archiveOldPayments() {
        MDC.put("operation", "archiveOldPayments");

        logger.info("Starting old payment archival process");

        try {
            // In a real system, you might move these to an archive table
            // For this example, we'll just count them
            LocalDateTime archiveDate = LocalDateTime.now().minusDays(90);
            long oldPaymentsCount = paymentRepository.countOldCompletedPayments(archiveDate);

            logger.info("Found {} old completed payments eligible for archival", oldPaymentsCount);

            // Here you would implement the actual archival logic
            // For example: move to archive table, export to S3, etc.

            return (int) oldPaymentsCount;

        } finally {
            MDC.remove("operation");
        }
    }
}
