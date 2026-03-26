package com.hackhaton.fctwaterbilling.service;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.entity.Invoice;
import com.hackhaton.fctwaterbilling.entity.MeterReading;
import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.entity.Tariff;
import com.hackhaton.fctwaterbilling.enums.InvoiceStatus;
import com.hackhaton.fctwaterbilling.enums.TariffTier;
import com.hackhaton.fctwaterbilling.repository.InvoiceRepository;
import com.hackhaton.fctwaterbilling.repository.MeterReadingRepository;
import com.hackhaton.fctwaterbilling.repository.SystemUserRepository;
import com.hackhaton.fctwaterbilling.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final int DUE_DAYS = 30;

    private final InvoiceRepository invoiceRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final TariffRepository tariffRepository;
    private final SystemUserRepository systemUserRepository;

    @Transactional(readOnly = true)
    public List<Invoice> listAll() {
        return invoiceRepository.findAllForListing();
    }

    /**
     * Creates one invoice per meter reading that is not yet linked to an invoice.
     * Consumption is the difference between this reading and the prior read on the same meter
     * (or zero baseline for the first read). Uses the active metered tariff for the customer's house type.
     */
    @Transactional
    public InvoiceBatchResult generateInvoicesFromMeterReadings(Long generatedByUserId) {
        List<MeterReading> pending = meterReadingRepository.findAllWithoutInvoiceFetched();
        if (pending.isEmpty()) {
            return new InvoiceBatchResult(0, 0, List.of("No uninvoiced meter readings."));
        }

        Map<Long, List<MeterReading>> historyByMeter = new HashMap<>();
        for (Long meterId : pending.stream().map(mr -> mr.getMeter().getId()).distinct().toList()) {
            historyByMeter.put(meterId, meterReadingRepository.findByMeter_IdOrderByReadAtAsc(meterId));
        }

        SystemUser generatedBy = null;
        if (generatedByUserId != null) {
            generatedBy = systemUserRepository.findById(generatedByUserId).orElse(null);
        }

        int created = 0;
        int skipped = 0;
        List<String> notes = new ArrayList<>();

        for (MeterReading mr : pending) {
            if (invoiceRepository.existsByMeterReading_Id(mr.getId())) {
                skipped++;
                continue;
            }

            CustomerAccount customer = mr.getMeter().getCustomerAccount();
            Optional<Tariff> tariffOpt = tariffRepository.findFirstByHouseTypeAndTariffTierAndIsActiveTrueOrderByIdAsc(
                    customer.getHouseType(), TariffTier.METER);

            if (tariffOpt.isEmpty()) {
                skipped++;
                notes.add("No active metered tariff for house type "
                        + customer.getHouseType() + " (reading id " + mr.getId() + ").");
                continue;
            }
            Tariff tariff = tariffOpt.get();

            List<MeterReading> history = historyByMeter.get(mr.getMeter().getId());
            int idx = -1;
            for (int i = 0; i < history.size(); i++) {
                if (history.get(i).getId().equals(mr.getId())) {
                    idx = i;
                    break;
                }
            }
            if (idx < 0) {
                skipped++;
                continue;
            }

            BigDecimal prevValue = idx > 0 ? history.get(idx - 1).getReadingValue() : BigDecimal.ZERO;
            BigDecimal consumption = mr.getReadingValue().subtract(prevValue);
            if (consumption.compareTo(BigDecimal.ZERO) < 0) {
                skipped++;
                notes.add("Negative consumption skipped for reading id " + mr.getId() + ".");
                continue;
            }

            BigDecimal rate = tariff.getRatePerUnit() != null ? tariff.getRatePerUnit() : BigDecimal.ZERO;
            BigDecimal consumptionCharge = consumption.multiply(rate).setScale(2, RoundingMode.HALF_UP);

            LocalDate periodStart = idx > 0
                    ? history.get(idx - 1).getReadAt().toLocalDate()
                    : mr.getMeter().getInstalledAt().toLocalDate();
            LocalDate periodEnd = mr.getReadAt().toLocalDate();

            String invoiceNumber = "INV-" + mr.getId();

            Invoice inv = Invoice.builder()
                    .customerAccount(customer)
                    .meterReading(mr)
                    .tariff(tariff)
                    .invoiceNumber(invoiceNumber)
                    .billingPeriodStart(periodStart)
                    .billingPeriodEnd(periodEnd)
                    .consumption(consumption)
                    .consumptionCharge(consumptionCharge)
                    .totalAmount(consumptionCharge)
                    .amountPaid(BigDecimal.ZERO)
                    .status(InvoiceStatus.ISSUED)
                    .dueDate(periodEnd.plusDays(DUE_DAYS))
                    .generatedAt(OffsetDateTime.now())
                    .generatedBy(generatedBy)
                    .build();

            invoiceRepository.save(inv);
            created++;
        }

        if (created > 0) {
            notes.add(0, "Created " + created + " invoice(s).");
        }
        if (skipped > 0 && created == 0 && notes.stream().noneMatch(s -> s.contains("No active metered"))) {
            notes.add(0, "Skipped " + skipped + " reading(s); see details below.");
        }

        return new InvoiceBatchResult(created, skipped, List.copyOf(notes));
    }

    public record InvoiceBatchResult(int created, int skipped, List<String> messages) {}
}
