package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.hackhaton.fctwaterbilling.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "customer_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private UserRole role;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 150)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "billing_address", nullable = false, columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "service_address", nullable = false, columnDefinition = "TEXT")
    private String serviceAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private OffsetDateTime registeredAt = OffsetDateTime.now();

    @Column(name = "deactivated_at")
    private OffsetDateTime deactivatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private CustomerAccount createdBy;

    @OneToOne(mappedBy = "customerAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private PinCredential pinCredential;

    // Flattened preferences to avoid maintaining a separate one-to-one table.
    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = false;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage = "en";

    @Column(name = "bill_ready_alert", nullable = false)
    private boolean billReadyAlert = true;

    @Column(name = "payment_alert", nullable = false)
    private boolean paymentAlert = true;

    @Column(name = "due_date_alert", nullable = false)
    private boolean dueDateAlert = true;

    @Column(name = "disconnect_alert", nullable = false)
    private boolean disconnectAlert = true;

    @OneToMany(mappedBy = "customerAccount", cascade = CascadeType.ALL)
    private List<Meter> meters;
}

