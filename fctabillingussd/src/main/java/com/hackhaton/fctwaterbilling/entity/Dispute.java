package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "dispute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id", nullable = false)
    private CustomerAccount customerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_reading_id")
    private MeterReading meterReading;

    @Column(name = "dispute_reference", nullable = false, unique = true, length = 50)
    private String disputeReference;

    @Column(name = "reason_code", nullable = false, length = 50)
    private String reasonCode;

    @Column(name = "reason_description", nullable = false, length = 255)
    private String reasonDescription;

    @Column(name = "customer_note", length = 500)
    private String customerNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private DisputeStatus status = DisputeStatus.SUBMITTED;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private SystemUser resolvedBy;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private OffsetDateTime submittedAt = OffsetDateTime.now();

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;
}
