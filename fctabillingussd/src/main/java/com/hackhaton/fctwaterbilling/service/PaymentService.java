package com.hackhaton.fctwaterbilling.service;

import com.hackhaton.fctwaterbilling.entity.Invoice;
import com.hackhaton.fctwaterbilling.entity.Payment;
import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.enums.InvoiceStatus;
import com.hackhaton.fctwaterbilling.enums.PaymentChannel;
import com.hackhaton.fctwaterbilling.enums.PaymentMethod;
import com.hackhaton.fctwaterbilling.enums.PaymentStatus;
import com.hackhaton.fctwaterbilling.repository.InvoiceRepository;
import com.hackhaton.fctwaterbilling.repository.PaymentRepository;
import com.hackhaton.fctwaterbilling.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SystemUserRepository systemUserRepository;

    @Transactional(readOnly = true)
    public List<Payment> listAll() {
        return paymentRepository.findAllForListing();
    }

    @Transactional(readOnly = true)
    public List<Invoice> listInvoicesOpenForPayment() {
        return invoiceRepository.findOpenForPayment(InvoiceStatus.VOID);
    }

    @Transactional(readOnly = true)
    public MonthlyPaymentSummary monthlySummaryUtc() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime start = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime end = start.plusMonths(1);

        long totalRecorded = paymentRepository.countRecordedBetween(start, end);
        BigDecimal amountCollected = paymentRepository.sumAmountByStatusBetween(PaymentStatus.SUCCESS, start, end);
        long completed = paymentRepository.countByStatusBetween(PaymentStatus.SUCCESS, start, end);

        return new MonthlyPaymentSummary(totalRecorded, amountCollected, completed);
    }

    @Transactional
    public Payment recordPayment(
            Long invoiceId,
            BigDecimal amount,
            PaymentMethod method,
            String notes,
            Long recordedByUserId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found."));
        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw new IllegalArgumentException("Cannot record payment for a voided invoice.");
        }

        BigDecimal total = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal remaining = total.subtract(paid);
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("This invoice has no outstanding balance.");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Amount exceeds outstanding balance (" + remaining.toPlainString() + ").");
        }

        String tempRef = "TMP-" + UUID.randomUUID();
        SystemUser recordedBy = null;
        if (recordedByUserId != null) {
            recordedBy = systemUserRepository.findById(recordedByUserId).orElse(null);
        }

        OffsetDateTime ts = OffsetDateTime.now(ZoneOffset.UTC);
        Payment payment = Payment.builder()
                .invoice(invoice)
                .paymentReference(tempRef)
                .amount(amount)
                .paymentMethod(method)
                .channel(PaymentChannel.ADMIN_PORTAL)
                .status(PaymentStatus.SUCCESS)
                .paidAt(ts)
                .recordedAt(ts)
                .recordedBy(recordedBy)
                .notes(notes != null && !notes.isBlank() ? notes.trim() : null)
                .build();

        payment = paymentRepository.save(payment);
        payment.setPaymentReference("PAY-" + String.format("%03d", payment.getId()));
        paymentRepository.save(payment);

        BigDecimal newPaid = paid.add(amount);
        invoice.setAmountPaid(newPaid);
        if (newPaid.compareTo(total) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        return payment;
    }

    public record MonthlyPaymentSummary(long totalPayments, BigDecimal amountCollected, long completedSuccessful) {}
}
