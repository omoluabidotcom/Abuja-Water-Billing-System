package com.hackhaton.fctwaterbilling.repository;

import com.hackhaton.fctwaterbilling.entity.Invoice;
import com.hackhaton.fctwaterbilling.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByMeterReading_Id(Long meterReadingId);

    boolean existsByCustomerAccount_IdAndBillingPeriodStartAndBillingPeriodEndAndMeterReadingIsNull(
            Long customerAccountId, LocalDate billingPeriodStart, LocalDate billingPeriodEnd);

    @Query("SELECT DISTINCT i FROM Invoice i JOIN FETCH i.customerAccount "
            + "WHERE i.status <> :voidStatus AND i.amountPaid < i.totalAmount ORDER BY i.generatedAt DESC")
    List<Invoice> findOpenForPayment(@Param("voidStatus") InvoiceStatus voidStatus);

    @Query("SELECT DISTINCT i FROM Invoice i "
            + "LEFT JOIN FETCH i.customerAccount "
            + "LEFT JOIN FETCH i.tariff "
            + "LEFT JOIN FETCH i.meterReading mr "
            + "LEFT JOIN FETCH mr.meter "
            + "ORDER BY i.generatedAt DESC")
    List<Invoice> findAllForListing();
}
