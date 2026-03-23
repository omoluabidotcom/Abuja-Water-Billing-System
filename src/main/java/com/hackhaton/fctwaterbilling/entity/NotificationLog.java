package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.NotifChannel;
import com.hackhaton.fctwaterbilling.enums.NotifStatus;
import com.hackhaton.fctwaterbilling.enums.NotifType;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id")
    private CustomerAccount customerAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private NotifChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, columnDefinition = "varchar(50)")
    private NotifType notificationType;

    @Column(nullable = false, length = 200)
    private String recipient;

    @Column(name = "message_body", nullable = false, columnDefinition = "TEXT")
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private NotifStatus status = NotifStatus.PENDING;

    @Column(name = "provider_ref", length = 255)
    private String providerRef;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}

