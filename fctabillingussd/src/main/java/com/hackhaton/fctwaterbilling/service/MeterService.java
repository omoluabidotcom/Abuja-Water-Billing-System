package com.hackhaton.fctwaterbilling.service;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.entity.Meter;
import com.hackhaton.fctwaterbilling.enums.MeterStatus;
import com.hackhaton.fctwaterbilling.repository.CustomerAccountRepository;
import com.hackhaton.fctwaterbilling.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerAccountRepository customerAccountRepository;

    public List<Meter> listForCustomer(Long customerAccountId) {
        return meterRepository.findByCustomerAccount_IdOrderByIdAsc(customerAccountId);
    }

    @Transactional
    public Meter registerMeter(Long customerAccountId, String meterId, String meterSerial, String locationDescription) {
        if (meterId == null || meterId.isBlank()) {
            throw new IllegalArgumentException("Meter ID is required.");
        }
        if (meterSerial == null || meterSerial.isBlank()) {
            throw new IllegalArgumentException("Meter serial is required.");
        }
        String mid = meterId.trim();
        String serial = meterSerial.trim();
        if (meterRepository.existsByMeterId(mid)) {
            throw new IllegalArgumentException("A meter with this meter ID already exists.");
        }
        if (meterRepository.existsByMeterSerial(serial)) {
            throw new IllegalArgumentException("A meter with this serial already exists.");
        }
        CustomerAccount account = customerAccountRepository.findById(customerAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));
        Meter meter = Meter.builder()
                .customerAccount(account)
                .meterId(mid)
                .meterSerial(serial)
                .locationDescription(locationDescription != null && !locationDescription.isBlank()
                        ? locationDescription.trim() : null)
                .status(MeterStatus.ACTIVE)
                .lastReading(BigDecimal.ZERO)
                .build();
        return meterRepository.save(meter);
    }
}
