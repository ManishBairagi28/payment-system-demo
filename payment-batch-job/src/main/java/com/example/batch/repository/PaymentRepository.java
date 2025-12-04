package com.example.batch.repository;

import com.example.batch.entity.Payment;
import com.example.batch.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /**
     * Find stale payments that should be expired
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :expiryDate")
    List<Payment> findStalePayments(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Count old completed payments for archival
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'COMPLETED' AND p.updatedAt < :archiveDate")
    long countOldCompletedPayments(@Param("archiveDate") LocalDateTime archiveDate);
}
