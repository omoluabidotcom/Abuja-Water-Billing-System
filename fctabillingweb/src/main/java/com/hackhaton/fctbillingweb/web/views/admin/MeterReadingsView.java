package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.entity.Meter;
import com.hackhaton.fctwaterbilling.entity.MeterReading;
import com.hackhaton.fctwaterbilling.enums.ReadingType;
import com.hackhaton.fctwaterbilling.service.CustomerAccountService;
import com.hackhaton.fctwaterbilling.service.MeterReadingService;
import com.hackhaton.fctwaterbilling.service.MeterService;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Route(value = "admin/meter-readings", layout = AdminLayout.class)
@PageTitle("Meter Readings – Water Utility Admin")
public class MeterReadingsView extends VerticalLayout implements BeforeEnterObserver {

    private final CustomerAccountService customerAccountService;
    private final MeterService meterService;
    private final MeterReadingService meterReadingService;

    private final ComboBox<CustomerAccount> customerCombo = new ComboBox<>("Customer");
    private final ComboBox<Meter> meterCombo = new ComboBox<>("Meter");
    private final BigDecimalField readingField = new BigDecimalField("Reading value");
    private final ComboBox<ReadingType> typeCombo = new ComboBox<>("Reading type");
    private final DateTimePicker readAtPicker = new DateTimePicker("Read date & time");
    private final Grid<MeterReading> grid = new Grid<>(MeterReading.class, false);
    private final Span lastReadingHint = new Span();
    private final Span emptyMetersHint = new Span();

    public MeterReadingsView(CustomerAccountService customerAccountService,
                             MeterService meterService,
                             MeterReadingService meterReadingService) {
        this.customerAccountService = customerAccountService;
        this.meterService = meterService;
        this.meterReadingService = meterReadingService;

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.STRETCH);
        getStyle().set("background", "#f1f5f9").set("box-sizing", "border-box");

        H2 title = new H2("Meter readings");
        title.getStyle().set("margin", "0").set("color", "#0f172a").set("font-size", "1.5rem");

        Paragraph subtitle = new Paragraph("Record meter reads for a customer and review recent history");
        subtitle.getStyle().set("margin", "0.25rem 0 0 0").set("color", "#64748b").set("font-size", "0.9rem");

        Button registerMeter = new Button("Register meter", VaadinIcon.PLUS_CIRCLE.create(), e -> openRegisterMeterDialog());
        registerMeter.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.END);
        VerticalLayout titles = new VerticalLayout(title, subtitle);
        titles.setPadding(false);
        titles.setSpacing(false);
        header.add(titles, registerMeter);

        customerCombo.setWidthFull();
        customerCombo.setItems(customerAccountService.listAll());
        customerCombo.setItemLabelGenerator(this::customerLabel);
        customerCombo.setPlaceholder("Select customer");
        customerCombo.addValueChangeListener(e -> onCustomerChanged());

        meterCombo.setWidthFull();
        meterCombo.setItemLabelGenerator(this::meterLabel);
        meterCombo.setPlaceholder("Select meter");
        meterCombo.addValueChangeListener(e -> onMeterChanged());

        emptyMetersHint.getStyle().set("font-size", "0.85rem").set("color", "#b45309").set("display", "none");
        emptyMetersHint.setText("This customer has no meters yet. Use “Register meter” to add one.");

        readingField.setWidthFull();
        readingField.setPlaceholder("e.g. 1250.5");

        typeCombo.setItems(ReadingType.values());
        typeCombo.setValue(ReadingType.ACTUAL);
        typeCombo.setItemLabelGenerator(t -> t.name().charAt(0) + t.name().substring(1).toLowerCase());
        typeCombo.setWidthFull();

        readAtPicker.setWidthFull();
        readAtPicker.setStep(java.time.Duration.ofMinutes(1));
        readAtPicker.setValue(LocalDateTime.now(ZoneOffset.UTC));

        lastReadingHint.getStyle().set("font-size", "0.8rem").set("color", "#64748b");

        Button save = new Button("Save reading", VaadinIcon.CHECK.create(), e -> saveReading());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.getStyle()
                .set("--lumo-primary-color", "#0f172a")
                .set("--lumo-primary-text-color", "#ffffff");

        configureGrid();
        grid.setWidthFull();
        grid.getStyle().set("min-width", "0");

        VerticalLayout formBlock = new VerticalLayout(
                customerCombo, meterCombo, emptyMetersHint, lastReadingHint,
                readingField, typeCombo, readAtPicker, save);
        formBlock.setPadding(false);
        formBlock.setSpacing(true);
        formBlock.setWidthFull();
        formBlock.setAlignItems(FlexComponent.Alignment.STRETCH);

        Div card = new Div();
        card.setWidthFull();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("padding", "1.25rem 1.5rem 1rem 1.5rem")
                .set("box-shadow", "0 2px 12px rgba(0,0,0,0.07)")
                .set("box-sizing", "border-box")
                .set("min-width", "0");

        VerticalLayout cardInner = new VerticalLayout(header, formBlock, grid);
        cardInner.setPadding(false);
        cardInner.setSpacing(true);
        cardInner.setWidthFull();
        cardInner.setAlignItems(FlexComponent.Alignment.STRETCH);
        card.add(cardInner);

        setFlexGrow(1, card);
        add(card);
        refreshCustomers();
    }

    private void configureGrid() {
        grid.addColumn(r -> r.getReadingValue()).setHeader("Value").setAutoWidth(true);
        grid.addColumn(r -> r.getReadingType().name()).setHeader("Type").setAutoWidth(true);
        grid.addColumn(r -> formatInstant(r.getReadAt())).setHeader("Read at").setFlexGrow(1);
        grid.addColumn(r -> formatInstant(r.getRecordedAt())).setHeader("Recorded at").setFlexGrow(1);
        grid.setAllRowsVisible(true);
    }

    private static String formatInstant(OffsetDateTime t) {
        return t != null ? t.toString() : "—";
    }

    private String customerLabel(CustomerAccount c) {
        return "ACC-" + c.getId() + " · " + c.getFirstName() + " " + c.getLastName();
    }

    private String meterLabel(Meter m) {
        String loc = m.getLocationDescription() != null && !m.getLocationDescription().isBlank()
                ? " — " + m.getLocationDescription()
                : "";
        return m.getMeterId() + " (" + m.getMeterSerial() + ")" + loc;
    }

    private void refreshCustomers() {
        List<CustomerAccount> list = customerAccountService.listAll();
        CustomerAccount prev = customerCombo.getValue();
        customerCombo.setItems(list);
        if (prev != null && list.stream().anyMatch(c -> c.getId().equals(prev.getId()))) {
            customerCombo.setValue(list.stream().filter(c -> c.getId().equals(prev.getId())).findFirst().orElse(null));
        }
        onCustomerChanged();
    }

    private void onCustomerChanged() {
        CustomerAccount c = customerCombo.getValue();
        meterCombo.clear();
        if (c == null) {
            emptyMetersHint.getStyle().set("display", "none");
            lastReadingHint.setText("");
            grid.setItems();
            return;
        }
        List<Meter> meters = meterService.listForCustomer(c.getId());
        meterCombo.setItems(meters);
        boolean empty = meters.isEmpty();
        emptyMetersHint.getStyle().set("display", empty ? "block" : "none");
        if (!meters.isEmpty() && meters.size() == 1) {
            meterCombo.setValue(meters.get(0));
        } else {
            meterCombo.clear();
        }
        onMeterChanged();
    }

    private void onMeterChanged() {
        Meter m = meterCombo.getValue();
        if (m == null) {
            lastReadingHint.setText("");
            grid.setItems();
            return;
        }
        lastReadingHint.setText("Last reading on file: " + m.getLastReading());
        refreshGrid();
    }

    private void refreshGrid() {
        Meter m = meterCombo.getValue();
        if (m == null) {
            grid.setItems();
            return;
        }
        grid.setItems(meterReadingService.listRecentForMeter(m.getId()));
    }

    private void saveReading() {
        CustomerAccount c = customerCombo.getValue();
        Meter m = meterCombo.getValue();
        if (c == null || m == null) {
            notifyError("Select a customer and meter.");
            return;
        }
        BigDecimal val = readingField.getValue();
        if (val == null) {
            notifyError("Enter a reading value.");
            return;
        }
        LocalDateTime ldt = readAtPicker.getValue();
        if (ldt == null) {
            notifyError("Choose read date and time.");
            return;
        }
        OffsetDateTime readAt = ldt.atOffset(ZoneOffset.UTC);
        Long uid = SecuritySession.getUser() != null ? SecuritySession.getUser().getId() : null;
        try {
            meterReadingService.recordReading(m.getId(), val, typeCombo.getValue(), readAt, uid);
            notifyOk("Reading saved.");
            readAtPicker.setValue(LocalDateTime.now(ZoneOffset.UTC));
            List<Meter> updated = meterService.listForCustomer(c.getId());
            meterCombo.setItems(updated);
            Meter refreshed = updated.stream().filter(x -> x.getId().equals(m.getId())).findFirst().orElse(m);
            meterCombo.setValue(refreshed);
            refreshGrid();
            lastReadingHint.setText("Last reading on file: " + refreshed.getLastReading());
        } catch (IllegalArgumentException ex) {
            notifyError(ex.getMessage());
        }
    }

    private void openRegisterMeterDialog() {
        CustomerAccount c = customerCombo.getValue();
        if (c == null) {
            notifyError("Select a customer first.");
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Register meter");

        TextField meterId = new TextField("Meter ID");
        meterId.setRequired(true);
        meterId.setWidthFull();
        meterId.setPlaceholder("e.g. MTR-00123");

        TextField serial = new TextField("Serial number");
        serial.setRequired(true);
        serial.setWidthFull();

        TextField location = new TextField("Location (optional)");
        location.setWidthFull();

        dialog.add(new VerticalLayout(meterId, serial, location));

        Button save = new Button("Save", e -> {
            try {
                meterService.registerMeter(c.getId(), meterId.getValue(), serial.getValue(), location.getValue());
                dialog.close();
                notifyOk("Meter registered.");
                refreshCustomers();
                customerCombo.setValue(c);
            } catch (IllegalArgumentException ex) {
                notifyError(ex.getMessage());
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
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
