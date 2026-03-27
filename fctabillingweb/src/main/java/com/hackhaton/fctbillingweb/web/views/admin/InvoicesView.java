package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctwaterbilling.entity.Invoice;
import com.hackhaton.fctwaterbilling.service.InvoiceService;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;

@Route(value = "admin/invoices", layout = AdminLayout.class)
@PageTitle("Invoices – Water Utility Admin")
public class InvoicesView extends VerticalLayout implements BeforeEnterObserver {

    private final InvoiceService invoiceService;

    private final Grid<Invoice> grid = new Grid<>(Invoice.class, false);

    public InvoicesView(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.STRETCH);
        getStyle().set("background", "#f1f5f9").set("box-sizing", "border-box");

        H2 title = new H2("Invoices");
        title.getStyle().set("margin", "0").set("color", "#0f172a").set("font-size", "1.5rem");

        Paragraph subtitle = new Paragraph(
                "Review issued bills. Generate from meter reads, or from estimated (flat) tariffs for customers on estimated billing.");
        subtitle.getStyle().set("margin", "0.25rem 0 0 0").set("color", "#64748b").set("font-size", "0.9rem");

        Button generateFromMeters = new Button("Generate from meter readings", VaadinIcon.FILE_ADD.create(), e -> generateInvoices());
        generateFromMeters.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generateFromMeters.getStyle()
                .set("--lumo-primary-color", "#0f172a")
                .set("--lumo-primary-text-color", "#ffffff");

        Button generateEstimated = new Button("Generate estimated (fixed tariff)", VaadinIcon.CALENDAR_CLOCK.create(),
                e -> generateEstimatedInvoices());
        generateEstimated.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        HorizontalLayout actions = new HorizontalLayout(generateFromMeters, generateEstimated);
        actions.setSpacing(true);
        actions.setAlignItems(FlexComponent.Alignment.END);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.END);
        VerticalLayout titles = new VerticalLayout(title, subtitle);
        titles.setPadding(false);
        titles.setSpacing(false);
        header.add(titles, actions);

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

        VerticalLayout cardInner = new VerticalLayout(header, grid);
        cardInner.setPadding(false);
        cardInner.setSpacing(true);
        cardInner.setWidthFull();
        cardInner.setAlignItems(FlexComponent.Alignment.STRETCH);
        card.add(cardInner);

        setFlexGrow(1, card);
        add(card);
        refreshGrid();
    }

    private void configureGrid() {
        grid.addColumn(Invoice::getInvoiceNumber).setHeader("Invoice #").setAutoWidth(true);
        grid.addColumn(this::customerName).setHeader("Customer").setFlexGrow(1);
        grid.addColumn(
                        inv -> inv.getBillingPeriodStart() + " → " + inv.getBillingPeriodEnd())
                .setHeader("Billing period")
                .setAutoWidth(true);
        grid.addColumn(this::consumption).setHeader("Consumption").setAutoWidth(true);
        grid.addColumn(this::totalAmount).setHeader("Total").setAutoWidth(true);
        grid.addColumn(inv -> inv.getStatus().name()).setHeader("Status").setAutoWidth(true);
        grid.addColumn(inv -> inv.getDueDate() != null ? inv.getDueDate().toString() : "—")
                .setHeader("Due")
                .setAutoWidth(true);
        grid.addColumn(inv -> inv.getGeneratedAt() != null ? inv.getGeneratedAt().toString() : "—")
                .setHeader("Generated")
                .setFlexGrow(1);
        grid.setAllRowsVisible(true);
    }

    private String customerName(Invoice inv) {
        if (inv.getCustomerAccount() == null) {
            return "—";
        }
        var c = inv.getCustomerAccount();
        return c.getFirstName() + " " + c.getLastName();
    }

    private String consumption(Invoice inv) {
        BigDecimal c = inv.getConsumption();
        return c != null ? c.toPlainString() : "—";
    }

    private String totalAmount(Invoice inv) {
        BigDecimal t = inv.getTotalAmount();
        return t != null ? t.toPlainString() : "—";
    }

    private void refreshGrid() {
        grid.setItems(invoiceService.listAll());
    }

    private void generateInvoices() {
        Long uid = SecuritySession.getUser() != null ? SecuritySession.getUser().getId() : null;
        InvoiceService.InvoiceBatchResult result = invoiceService.generateInvoicesFromMeterReadings(uid);
        refreshGrid();
        showBatchNotification(result);
    }

    private void generateEstimatedInvoices() {
        Long uid = SecuritySession.getUser() != null ? SecuritySession.getUser().getId() : null;
        InvoiceService.InvoiceBatchResult result = invoiceService.generateInvoicesForEstimatedCustomers(uid);
        refreshGrid();
        showBatchNotification(result);
    }

    private static void showBatchNotification(InvoiceService.InvoiceBatchResult result) {
        if (result.created() > 0) {
            Notification.show("Created " + result.created() + " invoice(s).", 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else if (result.skipped() > 0 && result.messages().size() > 1) {
            Notification.show(
                    String.join(" ", result.messages().subList(0, Math.min(3, result.messages().size()))),
                    6000,
                    Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        } else {
            Notification.show(
                    result.messages().isEmpty() ? "Nothing to do." : result.messages().get(0),
                    5000,
                    Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        }
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
