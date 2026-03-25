package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.AccountStatus;
import com.hackhaton.fctwaterbilling.enums.HouseType;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "customer_account")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SystemUser user;

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
    @Column(name = "account_status", nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "house_type", nullable = false, columnDefinition = "varchar(50)")
    private HouseType houseType;

    @Column(name = "is_estimated")
    private boolean isestimated;

    @Column(name = "registered_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime registeredAt = OffsetDateTime.now();

    @Column(name = "deactivated_at")
    private OffsetDateTime deactivatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin")
    private SystemUser createdByAdmin;

    @OneToOne(mappedBy = "customerAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private PinCredential pinCredential;

    // Flattened notification preferences to avoid maintaining a separate one-to-one table.
    @Column(name = "sms_enabled", nullable = false)
    @Builder.Default
    private boolean smsEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private boolean emailEnabled = false;

//    @Column(name = "preferred_language", nullable = false, length = 10)
//    @Builder.Default
//    private String preferredLanguage = "en";

    @Column(name = "due_date_alert", nullable = false)
    @Builder.Default
    private boolean dueDateAlert = true;

    @Column(name = "disconnect_alert", nullable = false)
    @Builder.Default
    private boolean disconnectAlert = true;

    @OneToMany(mappedBy = "customerAccount", cascade = CascadeType.ALL)
    private List<Meter> meters;
}
