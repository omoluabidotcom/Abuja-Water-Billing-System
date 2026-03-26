package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.entity.Invoice;
import com.hackhaton.fctwaterbilling.entity.Payment;
import com.hackhaton.fctwaterbilling.enums.PaymentMethod;
import com.hackhaton.fctwaterbilling.enums.PaymentStatus;
import com.hackhaton.fctwaterbilling.service.PaymentService;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

@Route(value = "admin/payments", layout = AdminLayout.class)
@PageTitle("Payment Reconciliation – Water Utility Admin")
public class PaymentsView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final PaymentService paymentService;

    private final Grid<Payment> grid = new Grid<>(Payment.class, false);
    private final Span statTotal = new Span("0");
    private final Span statAmount = new Span(formatNaira(BigDecimal.ZERO));
    private final Span statCompleted = new Span("0");

    public PaymentsView(PaymentService paymentService) {
        this.paymentService = paymentService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#f1f5f9").set("box-sizing", "border-box");

        H2 title = new H2("Payment Reconciliation");
        title.getStyle().set("margin", "0").set("color", "#0f172a").set("font-size", "1.5rem");

        Paragraph subtitle = new Paragraph("Record and track customer payments");
        subtitle.getStyle().set("margin", "0.25rem 0 0 0").set("color", "#64748b").set("font-size", "0.9rem");

        Button record = new Button("+ Record Payment", e -> openRecordDialog());
        record.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        record.getStyle()
                .set("--lumo-primary-color", "#0f172a")
                .set("--lumo-primary-text-color", "#ffffff");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.END);
        VerticalLayout titles = new VerticalLayout(title, subtitle);
        titles.setPadding(false);
        titles.setSpacing(false);
        header.add(titles, record);

        HorizontalLayout stats = new HorizontalLayout(
                summaryCard("Total Payments", "This month", VaadinIcon.CREDIT_CARD, "#2563eb", statTotal),
                summaryCard("Amount Collected", "Across all payment methods", VaadinIcon.DOLLAR, "#16a34a", statAmount),
                summaryCard("Completed", "Successful transactions", VaadinIcon.CHECK_CIRCLE, "#16a34a", statCompleted));
        stats.setWidthFull();
        stats.setSpacing(true);
        stats.getStyle().set("flex-wrap", "wrap");

        configureGrid();

        Div tableCard = new Div();
        tableCard.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "1.25rem 1.5rem 1rem 1.5rem")
                .set("box-shadow", "0 2px 12px rgba(0,0,0,0.07)");

        H2 recentTitle = new H2("Recent Payments");
        recentTitle.getStyle().set("margin", "0 0 0.75rem 0").set("color", "#0f172a").set("font-size", "1.1rem");

        VerticalLayout tableInner = new VerticalLayout(recentTitle, grid);
        tableInner.setPadding(false);
        tableInner.setSpacing(true);
        tableCard.add(tableInner);

        add(header, stats, tableCard);
        refreshAll();
    }

    private static Div summaryCard(String label, String footer, VaadinIcon icon, String iconColor, Span valueSpan) {
        Span lab = new Span(label);
        lab.getStyle().set("color", "#64748b").set("font-size", "0.85rem");

        Div iconWrap = new Div(icon.create());
        iconWrap.getStyle()
                .set("color", iconColor)
                .set("display", "flex")
                .set("align-items", "center");

        HorizontalLayout top = new HorizontalLayout(lab, iconWrap);
        top.setWidthFull();
        top.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        top.setAlignItems(FlexComponent.Alignment.CENTER);

        valueSpan.getStyle()
                .set("font-size", "1.75rem")
                .set("font-weight", "700")
                .set("color", "#0f172a")
                .set("display", "block");

        Span foot = new Span(footer);
        foot.getStyle().set("color", "#94a3b8").set("font-size", "0.78rem");

        VerticalLayout col = new VerticalLayout(top, valueSpan, foot);
        col.setPadding(false);
        col.setSpacing(false);
        col.getStyle().set("gap", "0.35rem");

        Div card = new Div(col);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "1rem 1.15rem")
                .set("border", "1px solid #e2e8f0")
                .set("box-shadow", "0 1px 8px rgba(0,0,0,0.04)")
                .set("flex", "1 1 200px")
                .set("min-width", "180px");
        return card;
    }

    private void configureGrid() {
        grid.addColumn(Payment::getPaymentReference).setHeader("Payment ID").setAutoWidth(true);
        grid.addColumn(this::paymentDate).setHeader("Date").setAutoWidth(true);
        grid.addComponentColumn(this::customerCell).setHeader("Customer").setFlexGrow(1);
        grid.addColumn(this::invoiceNumber).setHeader("Invoice").setAutoWidth(true);
        grid.addComponentColumn(this::methodBadge).setHeader("Method").setAutoWidth(true);
        grid.addComponentColumn(this::amountCell).setHeader("Amount").setAutoWidth(true);
        grid.addComponentColumn(this::statusBadge).setHeader("Status").setAutoWidth(true);
        grid.addColumn(this::notesText).setHeader("Notes").setFlexGrow(1);
        grid.setAllRowsVisible(true);
    }

    private String paymentDate(Payment p) {
        OffsetDateTime t = p.getPaidAt() != null ? p.getPaidAt() : p.getRecordedAt();
        if (t == null) {
            return "—";
        }
        return DATE_FMT.format(t.atZoneSameInstant(ZoneOffset.UTC).toLocalDate());
    }

    private Div customerCell(Payment p) {
        Invoice inv = p.getInvoice();
        if (inv == null || inv.getCustomerAccount() == null) {
            return new Div(new Span("—"));
        }
        CustomerAccount c = inv.getCustomerAccount();
        Span name = new Span(c.getFirstName() + " " + c.getLastName());
        name.getStyle().set("font-weight", "600").set("color", "#0f172a").set("display", "block");
        Span acc = new Span("ACC-" + c.getId());
        acc.getStyle().set("font-size", "0.78rem").set("color", "#64748b").set("display", "block");
        Div d = new Div(name, acc);
        d.getStyle().set("line-height", "1.25");
        return d;
    }

    private String invoiceNumber(Payment p) {
        return p.getInvoice() != null ? p.getInvoice().getInvoiceNumber() : "—";
    }

    private Span methodBadge(Payment p) {
        Span s = new Span(formatMethod(p.getPaymentMethod()));
        s.getStyle()
                .set("background", "#f1f5f9")
                .set("color", "#475569")
                .set("padding", "0.2rem 0.65rem")
                .set("border-radius", "999px")
                .set("font-size", "0.78rem");
        return s;
    }

    private Span amountCell(Payment p) {
        BigDecimal a = p.getAmount();
        String t = formatNaira(a);
        Span s = new Span(t);
        s.getStyle().set("color", "#15803d").set("font-weight", "600");
        return s;
    }

    private Span statusBadge(Payment p) {
        String text = formatStatus(p.getStatus());
        Span s = new Span(text);
        boolean ok = p.getStatus() == PaymentStatus.SUCCESS;
        s.getStyle()
                .set("background", ok ? "#dcfce7" : "#f1f5f9")
                .set("color", ok ? "#166534" : "#475569")
                .set("padding", "0.2rem 0.65rem")
                .set("border-radius", "999px")
                .set("font-size", "0.78rem");
        return s;
    }

    private String notesText(Payment p) {
        String n = p.getNotes();
        return n != null && !n.isBlank() ? n : "—";
    }

    private static String formatMethod(PaymentMethod m) {
        if (m == null) {
            return "—";
        }
        return switch (m) {
            case CARD -> "credit card";
            case CASH -> "cash";
            case BANK_TRANSFER -> "bank transfer";
            case MTN_MOMO -> "MTN MoMo";
            case AIRTEL_MONEY -> "Airtel Money";
        };
    }

    private static String formatStatus(PaymentStatus s) {
        if (s == null) {
            return "—";
        }
        return switch (s) {
            case SUCCESS -> "completed";
            case PENDING -> "pending";
            case FAILED -> "failed";
            case REVERSED -> "reversed";
        };
    }

    private void refreshAll() {
        grid.setItems(paymentService.listAll());
        PaymentService.MonthlyPaymentSummary sum = paymentService.monthlySummaryUtc();
        statTotal.setText(String.valueOf(sum.totalPayments()));
        BigDecimal amt = sum.amountCollected() != null ? sum.amountCollected() : BigDecimal.ZERO;
        statAmount.setText(formatNaira(amt));
        statCompleted.setText(String.valueOf(sum.completedSuccessful()));
    }

    private void openRecordDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Record payment");
        dialog.setWidth("min(480px, 100vw)");

        ComboBox<Invoice> invoiceCombo = new ComboBox<>("Invoice");
        invoiceCombo.setWidthFull();
        invoiceCombo.setItems(paymentService.listInvoicesOpenForPayment());
        invoiceCombo.setItemLabelGenerator(this::invoiceLabel);
        invoiceCombo.setPlaceholder("Select an invoice with a balance");

        BigDecimalField amountField = new BigDecimalField("Amount (NGN)");
        amountField.setWidthFull();
        amountField.setPlaceholder("0.00");
        amountField.setPrefixComponent(new Span("₦"));

        ComboBox<PaymentMethod> methodCombo = new ComboBox<>("Payment method");
        methodCombo.setWidthFull();
        methodCombo.setItems(PaymentMethod.values());
        methodCombo.setItemLabelGenerator(PaymentsView::formatMethod);
        methodCombo.setValue(PaymentMethod.CASH);

        TextArea notesField = new TextArea("Notes (optional)");
        notesField.setWidthFull();
        notesField.setMaxLength(500);

        invoiceCombo.addValueChangeListener(e -> {
            Invoice inv = e.getValue();
            if (inv == null) {
                amountField.clear();
                return;
            }
            BigDecimal rem = remaining(inv);
            amountField.setValue(rem);
        });

        dialog.add(new VerticalLayout(invoiceCombo, amountField, methodCombo, notesField));

        Button save = new Button("Save", ev -> {
            Invoice inv = invoiceCombo.getValue();
            BigDecimal amt = amountField.getValue();
            PaymentMethod method = methodCombo.getValue();
            if (inv == null) {
                notifyError("Select an invoice.");
                return;
            }
            if (amt == null || amt.compareTo(BigDecimal.ZERO) <= 0) {
                notifyError("Enter a valid amount.");
                return;
            }
            if (method == null) {
                notifyError("Select a payment method.");
                return;
            }
            Long uid = SecuritySession.getUser() != null ? SecuritySession.getUser().getId() : null;
            try {
                paymentService.recordPayment(inv.getId(), amt, method, notesField.getValue(), uid);
                notifyOk("Payment recorded.");
                dialog.close();
                refreshAll();
            } catch (IllegalArgumentException ex) {
                notifyError(ex.getMessage());
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.getStyle().set("--lumo-primary-color", "#0f172a").set("--lumo-primary-text-color", "#ffffff");

        Button cancel = new Button("Cancel", ev -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private String invoiceLabel(Invoice i) {
        if (i.getCustomerAccount() == null) {
            return i.getInvoiceNumber();
        }
        var c = i.getCustomerAccount();
        return i.getInvoiceNumber() + " · " + c.getFirstName() + " " + c.getLastName()
                + " · balance " + formatNaira(remaining(i));
    }

    private static String formatNaira(BigDecimal amount) {
        if (amount == null) {
            return "—";
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-NG"));
        nf.setCurrency(Currency.getInstance("NGN"));
        return nf.format(amount);
    }

    private static BigDecimal remaining(Invoice i) {
        BigDecimal total = i.getTotalAmount() != null ? i.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = i.getAmountPaid() != null ? i.getAmountPaid() : BigDecimal.ZERO;
        return total.subtract(paid).max(BigDecimal.ZERO);
    }

    private static void notifyOk(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private static void notifyError(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!SecuritySession.isLoggedIn()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!SecuritySession.isAdmin()) {
            event.forwardTo(com.hackhaton.fctbillingweb.web.views.DashboardView.class);
        }
    }
}
