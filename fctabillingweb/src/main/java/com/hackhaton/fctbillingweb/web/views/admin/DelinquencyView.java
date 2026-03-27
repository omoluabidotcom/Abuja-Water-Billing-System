package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.entity.Invoice;
import com.hackhaton.fctwaterbilling.service.InvoiceService;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/delinquency", layout = AdminLayout.class)
@PageTitle("Delinquency – Water Utility Admin")
public class DelinquencyView extends VerticalLayout implements BeforeEnterObserver {

    private final InvoiceService invoiceService;

    private final Grid<Invoice> grid = new Grid<>(Invoice.class, false);
    private final Div statInvoices = statShell();
    private final Div statCustomers = statShell();
    private final Div statBalance = statShell();

    public DelinquencyView(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.STRETCH);
        getStyle().set("background", "#f1f5f9").set("box-sizing", "border-box");

        H2 title = new H2("Delinquency management");
        title.getStyle().set("margin", "0").set("color", "#0f172a").set("font-size", "1.5rem");

        Paragraph subtitle = new Paragraph(
                "Invoices that are not fully paid and are past their due date (voided bills are excluded). "
                        + "Use this list for follow-up, disconnect planning, and collections.");
        subtitle.getStyle().set("margin", "0.25rem 0 0 0").set("color", "#64748b").set("font-size", "0.9rem");

        Button refresh = new Button("Refresh", VaadinIcon.REFRESH.create(), e -> refreshAll());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refresh.getStyle()
                .set("--lumo-primary-color", "#0f172a")
                .set("--lumo-primary-text-color", "#ffffff");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.END);
        VerticalLayout titles = new VerticalLayout(title, subtitle);
        titles.setPadding(false);
        titles.setSpacing(false);
        header.add(titles, refresh);

        HorizontalLayout kpiRow = new HorizontalLayout(statInvoices, statCustomers, statBalance);
        kpiRow.setWidthFull();
        kpiRow.setSpacing(true);
        kpiRow.getStyle().set("flex-wrap", "wrap");

        configureGrid();
        grid.setWidthFull();
        grid.getStyle().set("min-width", "0");

        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "1.25rem 1.5rem 1rem 1.5rem")
                .set("box-shadow", "0 2px 12px rgba(0,0,0,0.07)")
                .set("box-sizing", "border-box")
                .set("min-width", "0");

        VerticalLayout cardInner = new VerticalLayout(header, kpiRow, grid);
        cardInner.setPadding(false);
        cardInner.setSpacing(true);
        cardInner.setWidthFull();
        cardInner.setAlignItems(FlexComponent.Alignment.STRETCH);
        card.add(cardInner);

        setFlexGrow(1, card);
        add(card);

        refreshAll();
    }

    private static Div statShell() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "12px")
                .set("padding", "1rem 1.25rem")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.06)")
                .set("flex", "1 1 180px")
                .set("min-width", "160px")
                .set("border-left", "4px solid #dc2626");
        return card;
    }

    private void configureGrid() {
        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice #").setAutoWidth(true);
        grid.addColumn(this::customerName).setHeader("Customer").setFlexGrow(1);
        grid.addColumn(c -> accountPhone(c.getCustomerAccount())).setHeader("Phone").setAutoWidth(true);
        grid.addColumn(inv -> inv.getBillingPeriodStart() + " → " + inv.getBillingPeriodEnd())
                .setHeader("Billing period")
                .setAutoWidth(true);
        grid.addColumn(inv -> inv.getDueDate() != null ? inv.getDueDate().toString() : "—")
                .setHeader("Due date")
                .setAutoWidth(true);
        grid.addColumn(this::daysPastDue).setHeader("Days past due").setAutoWidth(true);
        grid.addColumn(this::balanceDue).setHeader("Balance due").setAutoWidth(true);
        grid.addColumn(inv -> inv.getStatus().name()).setHeader("Status").setAutoWidth(true);
        grid.setAllRowsVisible(true);
    }

    private String customerName(Invoice inv) {
        if (inv.getCustomerAccount() == null) {
            return "—";
        }
        var c = inv.getCustomerAccount();
        return c.getFirstName() + " " + c.getLastName();
    }

    private static String accountPhone(CustomerAccount c) {
        if (c == null || c.getPhoneNumber() == null || c.getPhoneNumber().isBlank()) {
            return "—";
        }
        return c.getPhoneNumber();
    }

    private String daysPastDue(Invoice inv) {
        if (inv.getDueDate() == null) {
            return "—";
        }
        long d = ChronoUnit.DAYS.between(inv.getDueDate(), LocalDate.now());
        return d < 0 ? "0" : Long.toString(d);
    }

    private String balanceDue(Invoice inv) {
        BigDecimal bal = balance(inv);
        return bal != null ? bal.toPlainString() : "—";
    }

    private static BigDecimal balance(Invoice inv) {
        if (inv.getTotalAmount() == null) {
            return null;
        }
        BigDecimal paid = inv.getAmountPaid() != null ? inv.getAmountPaid() : BigDecimal.ZERO;
        return inv.getTotalAmount().subtract(paid).max(BigDecimal.ZERO);
    }

    private void refreshAll() {
        List<Invoice> delinquent = invoiceService.listDelinquent();
        grid.setItems(delinquent);

        long invoiceCount = delinquent.size();
        long customerCount = delinquent.stream()
                .map(Invoice::getCustomerAccount)
                .filter(c -> c != null && c.getId() != null)
                .map(CustomerAccount::getId)
                .collect(Collectors.toSet())
                .size();
        BigDecimal totalBalance = delinquent.stream()
                .map(DelinquencyView::balance)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        fillStatCard(statInvoices, "Past-due invoices", Long.toString(invoiceCount),
                "Open balance after due date");
        fillStatCard(statCustomers, "Affected customers", Long.toString(customerCount),
                "Distinct accounts with at least one past-due bill");
        fillStatCard(statBalance, "Total outstanding", totalBalance.toPlainString(),
                "Sum of balance due on listed invoices");
    }

    private static void fillStatCard(Div shell, String title, String value, String hint) {
        shell.removeAll();
        H3 h = new H3(title);
        h.getStyle()
                .set("margin", "0 0 0.35rem 0")
                .set("font-size", "0.85rem")
                .set("color", "#64748b")
                .set("font-weight", "600");
        Paragraph v = new Paragraph(value);
        v.getStyle()
                .set("margin", "0 0 0.35rem 0")
                .set("font-size", "1.35rem")
                .set("color", "#0f172a")
                .set("font-weight", "700");
        Paragraph sub = new Paragraph(hint);
        sub.getStyle().set("margin", "0").set("font-size", "0.75rem").set("color", "#94a3b8");
        shell.add(h, v, sub);
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
