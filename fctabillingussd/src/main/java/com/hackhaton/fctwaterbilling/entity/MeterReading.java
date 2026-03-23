package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.ReadingSource;
import com.hackhaton.fctwaterbilling.enums.ReadingType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meter_reading")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private MeterReadingBatch batch;

    @Column(name = "reading_value", nullable = false, precision = 12, scale = 3)
    private BigDecimal readingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private ReadingType readingType = ReadingType.ACTUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private ReadingSource source = ReadingSource.MANUAL;


    @Column(name = "is_disputed", nullable = false)
    @Builder.Default
    private boolean isDisputed = false;

    @Column(name = "read_at", nullable = false)
    private OffsetDateTime readAt;

    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private SystemUser recordedBy;
}
