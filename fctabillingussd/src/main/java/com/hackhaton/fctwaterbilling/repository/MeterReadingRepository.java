package com.hackhaton.fctwaterbilling.repository;

import com.hackhaton.fctwaterbilling.entity.MeterReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    List<MeterReading> findByMeter_IdOrderByReadAtDesc(Long meterId, Pageable pageable);

    List<MeterReading> findByMeter_IdOrderByReadAtAsc(Long meterId);

    @Query("SELECT mr FROM MeterReading mr "
            + "JOIN FETCH mr.meter m "
            + "JOIN FETCH m.customerAccount "
            + "WHERE NOT EXISTS (SELECT 1 FROM Invoice i WHERE i.meterReading = mr) "
            + "ORDER BY m.id, mr.readAt")
    List<MeterReading> findAllWithoutInvoiceFetched();
}
