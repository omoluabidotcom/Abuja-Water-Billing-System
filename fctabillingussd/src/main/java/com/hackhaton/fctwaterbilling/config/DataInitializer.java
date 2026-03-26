package com.hackhaton.fctwaterbilling.config;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.entity.Meter;
import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.entity.Tariff;
import com.hackhaton.fctwaterbilling.enums.AccountStatus;
import com.hackhaton.fctwaterbilling.enums.HouseType;
import com.hackhaton.fctwaterbilling.enums.MeterStatus;
import com.hackhaton.fctwaterbilling.enums.TariffTier;
import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.hackhaton.fctwaterbilling.enums.UserStatus;
import com.hackhaton.fctwaterbilling.repository.CustomerAccountRepository;
import com.hackhaton.fctwaterbilling.repository.MeterRepository;
import com.hackhaton.fctwaterbilling.repository.SystemUserRepository;
import com.hackhaton.fctwaterbilling.repository.TariffRepository;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SystemUserRepository userRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final MeterRepository meterRepository;
    private final TariffRepository tariffRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdminUser();
        seedSampleCustomers();
        seedSampleMeters();
        seedSampleTariffs();
    }

    private void seedAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Admin user already exists — skipping seed.");
            return;
        }

        SystemUser admin = SystemUser.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .createdBy("system")
                .build();

        userRepository.save(admin);
        log.info("Default admin user created successfully.");
    }

    private void seedSampleCustomers() {
        if (customerAccountRepository.count() > 0) {
            return;
        }

        customerAccountRepository.save(CustomerAccount.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+234 803 000 1001")
                .billingAddress("12 Nile Street, Maitama, Abuja")
                .serviceAddress("12 Nile Street, Maitama, Abuja")
                .houseType(HouseType.TWOBEDEROOM)
                .accountStatus(AccountStatus.ACTIVE)
                .createdByAdmin(null)
                .build());

        customerAccountRepository.save(CustomerAccount.builder()
                .firstName("Amina")
                .lastName("Bello")
                .email("amina.bello@example.com")
                .phoneNumber("+234 802 000 2002")
                .billingAddress("45 Lake Chad Crescent, Wuse II")
                .serviceAddress("45 Lake Chad Crescent, Wuse II")
                .houseType(HouseType.ONEBEDROOM)
                .accountStatus(AccountStatus.SUSPENDED)
                .createdByAdmin(null)
                .build());

        customerAccountRepository.save(CustomerAccount.builder()
                .firstName("Chidi")
                .lastName("Okonkwo")
                .email("chidi.o@example.com")
                .phoneNumber("+234 901 000 3003")
                .billingAddress("8 Gimbiya Street, Area 11")
                .serviceAddress("Plot 22, Gwarinpa Estate")
                .houseType(HouseType.FOURBEDROOM)
                .accountStatus(AccountStatus.ACTIVE)
                .createdByAdmin(null)
                .build());

        log.info("Sample customer accounts seeded.");
    }

    private void seedSampleMeters() {
        if (meterRepository.count() > 0) {
            return;
        }
        List<CustomerAccount> customers = customerAccountRepository.findAllByOrderByIdAsc();
        if (customers.isEmpty()) {
            return;
        }
        for (CustomerAccount c : customers) {
            meterRepository.save(Meter.builder()
                    .customerAccount(c)
                    .meterId("MTR-" + String.format("%05d", c.getId()))
                    .meterSerial("SN-" + String.format("%05d", c.getId()))
                    .locationDescription("Primary meter")
                    .status(MeterStatus.ACTIVE)
                    .lastReading(BigDecimal.ZERO)
                    .build());
        }
        log.info("Sample meters seeded (one per customer).");
    }

    private void seedSampleTariffs() {
        if (tariffRepository.count() > 0) {
            return;
        }

        tariffRepository.save(Tariff.builder()
                .name("Metered — one bedroom")
                .description("Rate per gallon for metered one-bedroom connections.")
                .houseType(HouseType.ONEBEDROOM)
                .ratePerUnit(new BigDecimal("185.5000"))
                .tariffTier(TariffTier.METER)
                .isActive(true)
                .createdBy(null)
                .build());

        tariffRepository.save(Tariff.builder()
                .name("Metered — two bedroom")
                .description("Rate per gallon for metered two-bedroom connections.")
                .houseType(HouseType.TWOBEDEROOM)
                .ratePerUnit(new BigDecimal("210.0000"))
                .tariffTier(TariffTier.METER)
                .isActive(true)
                .createdBy(null)
                .build());

        tariffRepository.save(Tariff.builder()
                .name("Estimated — self-contain")
                .description("Flat estimated billing where no reliable meter read is available.")
                .houseType(HouseType.SELFCONTAIN)
                .ratePerUnit(BigDecimal.ZERO)
                .fixedTariff(new BigDecimal("12000.0000"))
                .tariffTier(TariffTier.ESTIMATED)
                .isActive(true)
                .createdBy(null)
                .build());

        log.info("Sample tariffs seeded.");
    }
}

