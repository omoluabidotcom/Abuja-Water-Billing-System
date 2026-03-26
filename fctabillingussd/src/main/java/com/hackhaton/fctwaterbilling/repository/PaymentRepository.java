package com.hackhaton.fctwaterbilling.repository;

import com.hackhaton.fctwaterbilling.entity.Payment;
import com.hackhaton.fctwaterbilling.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p "
            + "LEFT JOIN FETCH p.invoice i "
            + "LEFT JOIN FETCH i.customerAccount "
            + "ORDER BY p.recordedAt DESC")
    List<Payment> findAllForListing();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.recordedAt >= :start AND p.recordedAt < :end")
    long countRecordedBetween(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status "
            + "AND p.recordedAt >= :start AND p.recordedAt < :end")
    BigDecimal sumAmountByStatusBetween(
            @Param("status") PaymentStatus status,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status "
            + "AND p.recordedAt >= :start AND p.recordedAt < :end")
    long countByStatusBetween(
            @Param("status") PaymentStatus status,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);
}
