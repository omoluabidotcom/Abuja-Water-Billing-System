package com.hackhaton.fctwaterbilling.entity;

import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.hackhaton.fctwaterbilling.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "system_user")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private UserRole role = UserRole.ADMIN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
