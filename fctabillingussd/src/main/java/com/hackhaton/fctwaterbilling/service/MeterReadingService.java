package com.hackhaton.fctwaterbilling.service;

import com.hackhaton.fctwaterbilling.entity.Meter;
import com.hackhaton.fctwaterbilling.entity.MeterReading;
import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.enums.ReadingSource;
import com.hackhaton.fctwaterbilling.enums.ReadingType;
import com.hackhaton.fctwaterbilling.repository.MeterReadingRepository;
import com.hackhaton.fctwaterbilling.repository.MeterRepository;
import com.hackhaton.fctwaterbilling.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private static final int RECENT_PAGE_SIZE = 100;

    private final MeterRepository meterRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final SystemUserRepository systemUserRepository;

    public List<MeterReading> listRecentForMeter(Long meterId) {
        return meterReadingRepository.findByMeter_IdOrderByReadAtDesc(
                meterId, PageRequest.of(0, RECENT_PAGE_SIZE));
    }

    @Transactional
    public MeterReading recordReading(Long meterId,
                                      BigDecimal readingValue,
                                      ReadingType readingType,
                                      OffsetDateTime readAt,
                                      Long recordedByUserId) {
        if (readingValue == null) {
            throw new IllegalArgumentException("Reading value is required.");
        }
        if (readAt == null) {
            throw new IllegalArgumentException("Read date/time is required.");
        }
        if (readingValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Reading cannot be negative.");
        }
        Meter meter = meterRepository.findById(meterId)
                .orElseThrow(() -> new IllegalArgumentException("Meter not found."));
        if (readingValue.compareTo(meter.getLastReading()) < 0) {
            throw new IllegalArgumentException(
                    "Reading must be greater than or equal to the last reading (" + meter.getLastReading() + ").");
        }
        SystemUser recordedBy = null;
        if (recordedByUserId != null) {
            recordedBy = systemUserRepository.findById(recordedByUserId).orElse(null);
        }
        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .readingValue(readingValue)
                .readingType(readingType != null ? readingType : ReadingType.ACTUAL)
                .source(ReadingSource.ADMIN_PORTAL)
                .readAt(readAt)
                .recordedAt(OffsetDateTime.now())
                .recordedBy(recordedBy)
                .build();
        MeterReading saved = meterReadingRepository.save(reading);
        meter.setLastReading(readingValue);
        meterRepository.save(meter);
        return saved;
    }
}
