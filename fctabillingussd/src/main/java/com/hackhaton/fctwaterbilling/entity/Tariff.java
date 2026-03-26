package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.HouseType;
import com.hackhaton.fctwaterbilling.enums.TariffTier;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tariff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tariff extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "house_type", nullable = false, columnDefinition = "varchar(50)")
    private HouseType houseType;

    /** Metered price per gallon (DB column name retained as rate_per_unit). */
    @Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal ratePerUnit = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private TariffTier tariffTier = TariffTier.METER;

    /** Flat charge when {@link TariffTier#ESTIMATED}; ignored for metered tiers. */
    @Column(name = "fixed_tariff", nullable = false, precision = 12, scale = 4)
    @Builder.Default
    private BigDecimal fixedTariff = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private SystemUser createdBy;
}
