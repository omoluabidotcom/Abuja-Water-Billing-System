package com.hackhaton.fctwaterbilling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meter_reading_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReadingBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_reference", nullable = false, unique = true, length = 100)
    private String batchReference;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "total_records", nullable = false)
    private int totalRecords = 0;

    @Column(name = "success_count", nullable = false)
    private int successCount = 0;

    @Column(name = "error_count", nullable = false)
    private int errorCount = 0;

    @Column(nullable = false, length = 20)
    private String status = "PROCESSING";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private CustomerAccount uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;
}

