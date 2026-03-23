package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.MeterStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id", nullable = false)
    private CustomerAccount customerAccount;

    @Column(name = "meter_id", nullable = false, unique = true, length = 50)
    private String meterId;

    @Column(name = "meter_serial", nullable = false, unique = true, length = 100)
    private String meterSerial;

    @Column(name = "location_description", columnDefinition = "TEXT")
    private String locationDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private MeterStatus status = MeterStatus.ACTIVE;

    @Column(name = "last_reading", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal lastReading = BigDecimal.ZERO;

    @Column(name = "installed_at", nullable = false)
    @Builder.Default
    private OffsetDateTime installedAt = OffsetDateTime.now();

    @Column(name = "decommissioned_at")
    private OffsetDateTime decommissionedAt;
}
