package com.hackhaton.fctwaterbilling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pin_credential")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id", nullable = false, unique = true)
    private CustomerAccount customerAccount;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}

