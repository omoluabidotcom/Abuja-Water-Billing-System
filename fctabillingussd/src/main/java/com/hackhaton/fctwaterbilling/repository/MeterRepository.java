package com.hackhaton.fctwaterbilling.repository;

import com.hackhaton.fctwaterbilling.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    List<Meter> findByCustomerAccount_IdOrderByIdAsc(Long customerAccountId);

    boolean existsByMeterId(String meterId);

    boolean existsByMeterSerial(String meterSerial);

    Optional<Meter> findByMeterId(String meterId);
}
