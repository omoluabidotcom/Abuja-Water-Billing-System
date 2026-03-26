package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctwaterbilling.entity.CustomerAccount;
import com.hackhaton.fctwaterbilling.enums.AccountStatus;
import com.hackhaton.fctwaterbilling.enums.HouseType;
import com.hackhaton.fctwaterbilling.service.CustomerAccountService;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "admin/customers", layout = AdminLayout.class)
@PageTitle("Customers – Water Utility Admin")
public class CustomersView extends VerticalLayout implements BeforeEnterObserver {

    private final CustomerAccountService customerAccountService;
    private final Grid<CustomerAccount> grid = new Grid<>(CustomerAccount.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<AccountStatus> statusFilter = new ComboBox<>();
    private final Span footerCount = new Span();

    public CustomersView(CustomerAccountService customerAccountService) {
        this.customerAccountService = customerAccountService;

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.STRETCH);
        getStyle().set("background", "#f1f5f9").set("box-sizing", "border-box");

        H2 title = new H2("Customer Management");
        title.getStyle().set("margin", "0").set("color", "#0f172a").set("font-size", "1.5rem");

        Paragraph subtitle = new Paragraph("Manage customer accounts and meter assignments");
        subtitle.getStyle().set("margin", "0.25rem 0 0 0").set("color", "#64748b").set("font-size", "0.9rem");

        Button addCustomer = new Button("Add Customer", VaadinIcon.PLUS.create(), e -> openAddDialog());
        addCustomer.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addCustomer.getStyle()
                .set("--lumo-primary-color", "#0f172a")
                .set("--lumo-primary-text-color", "#ffffff");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.END);
        VerticalLayout titles = new VerticalLayout(title, subtitle);
        titles.setPadding(false);
        titles.setSpacing(false);
        header.add(titles, addCustomer);

        searchField.setPlaceholder("Search by name, account, email, or address…");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());

        statusFilter.setPlaceholder("All status");
        statusFilter.setItems(AccountStatus.values());
        statusFilter.setItemLabelGenerator(Enum::name);
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("200px");
        statusFilter.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, statusFilter);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.setAlignItems(FlexComponent.Alignment.END);

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

        VerticalLayout cardInner = new VerticalLayout(header, toolbar, grid, footerCount);
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
        grid.addColumn(c -> "ACC-" + c.getId()).setHeader("Account #").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::nameCell).setHeader("Customer Name").setFlexGrow(1);
        grid.addComponentColumn(this::contactCell).setHeader("Contact").setFlexGrow(1);
        grid.addComponentColumn(this::statusBadge).setHeader("Status").setAutoWidth(true).setFlexGrow(0);
        grid.setAllRowsVisible(true);
        grid.addClassName("customers-grid");
    }

    private VerticalLayout nameCell(CustomerAccount c) {
        Span name = new Span(c.getFirstName() + " " + c.getLastName());
        name.getStyle().set("font-weight", "700").set("color", "#0f172a").set("display", "block");
        Span addr = new Span(c.getServiceAddress() != null ? c.getServiceAddress() : "");
        addr.getStyle().set("font-size", "0.8rem").set("color", "#64748b").set("display", "block");
        VerticalLayout vl = new VerticalLayout(name, addr);
        vl.setPadding(false);
        vl.setSpacing(false);
        return vl;
    }

    private VerticalLayout contactCell(CustomerAccount c) {
        Div emailRow = contactRow(VaadinIcon.ENVELOPE.create(), c.getEmail() != null ? c.getEmail() : "—");
        Div phoneRow = contactRow(VaadinIcon.PHONE.create(), c.getPhoneNumber());
        VerticalLayout vl = new VerticalLayout(emailRow, phoneRow);
        vl.setPadding(false);
        vl.setSpacing(false);
        return vl;
    }

    private static Div contactRow(com.vaadin.flow.component.Component icon, String text) {
        icon.getElement().getStyle().set("color", "#94a3b8").set("width", "1rem");
        Span t = new Span(text);
        t.getStyle().set("font-size", "0.85rem").set("color", "#334155");
        HorizontalLayout row = new HorizontalLayout(icon, t);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.setPadding(false);
        Div wrap = new Div(row);
        wrap.getStyle().set("margin", "0");
        return wrap;
    }

    private Span statusBadge(CustomerAccount c) {
        Span pill = new Span(c.getAccountStatus().name().toLowerCase().replace('_', ' '));
        pill.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.2rem 0.65rem")
                .set("border-radius", "999px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("text-transform", "capitalize");

        switch (c.getAccountStatus()) {
            case ACTIVE -> pill.getStyle()
                    .set("background", "#0f172a")
                    .set("color", "white");
            case SUSPENDED -> pill.getStyle()
                    .set("background", "#dc2626")
                    .set("color", "white");
            default -> pill.getStyle()
                    .set("background", "#e2e8f0")
                    .set("color", "#475569");
        }
        return pill;
    }

    private void refreshGrid() {
        String q = searchField.getValue() != null ? searchField.getValue() : "";
        AccountStatus st = statusFilter.getValue();
        List<CustomerAccount> rows = customerAccountService.listFiltered(q, st);
        grid.setItems(rows);
        long total = customerAccountService.countAll();
        footerCount.setText("Showing " + rows.size() + " of " + total + " customers");
        footerCount.getStyle().set("font-size", "0.8rem").set("color", "#94a3b8").set("margin-top", "0.25rem");
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Add customer");

        TextField firstName = new TextField("First name");
        firstName.setRequired(true);
        firstName.setWidthFull();

        TextField lastName = new TextField("Last name");
        lastName.setRequired(true);
        lastName.setWidthFull();

        EmailField email = new EmailField("Email");
        email.setWidthFull();

        TextField phone = new TextField("Phone");
        phone.setRequired(true);
        phone.setWidthFull();

        TextArea billing = new TextArea("Billing address");
        billing.setRequired(true);
        billing.setWidthFull();

        TextArea service = new TextArea("Service address");
        service.setRequired(true);
        service.setWidthFull();

        ComboBox<HouseType> house = new ComboBox<>("House type");
        house.setItems(HouseType.values());
        house.setRequired(true);
        house.setWidthFull();
        house.setItemLabelGenerator(h -> h.name().replace('_', ' '));

        ComboBox<AccountStatus> status = new ComboBox<>("Account status");
        status.setItems(AccountStatus.values());
        status.setValue(AccountStatus.ACTIVE);
        status.setWidthFull();

        Checkbox isEstimated = new Checkbox("Estimated usage / billing (non-metered or estimated reads)");
        isEstimated.setWidthFull();

        VerticalLayout form = new VerticalLayout(firstName, lastName, email, phone, billing, service, house, status, isEstimated);
        form.setPadding(false);
        dialog.add(form);

        Button save = new Button("Save", e -> {
            if (!firstName.getValue().trim().isEmpty()
                    && !lastName.getValue().trim().isEmpty()
                    && !phone.getValue().trim().isEmpty()
                    && !billing.getValue().trim().isEmpty()
                    && !service.getValue().trim().isEmpty()
                    && house.getValue() != null) {
                CustomerAccount acc = CustomerAccount.builder()
                        .firstName(firstName.getValue().trim())
                        .lastName(lastName.getValue().trim())
                        .email(email.getValue() != null && !email.getValue().isBlank() ? email.getValue().trim() : null)
                        .phoneNumber(phone.getValue().trim())
                        .billingAddress(billing.getValue().trim())
                        .serviceAddress(service.getValue().trim())
                        .houseType(house.getValue())
                        .accountStatus(status.getValue() != null ? status.getValue() : AccountStatus.ACTIVE)
                        .isestimated(isEstimated.getValue())
                        .createdByAdmin(SecuritySession.getUser())
                        .build();
                customerAccountService.saveNew(acc);
                dialog.close();
                refreshGrid();
                Notification.show("Customer created.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification.show("Please fill all required fields.", 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
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
