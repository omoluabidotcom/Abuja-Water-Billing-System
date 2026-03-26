package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctwaterbilling.entity.Tariff;
import com.hackhaton.fctwaterbilling.enums.HouseType;
import com.hackhaton.fctwaterbilling.enums.TariffTier;
import com.hackhaton.fctwaterbilling.service.TariffService;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "admin/tariffs", layout = AdminLayout.class)
@PageTitle("Tariffs – Water Utility Admin")
public class TariffsView extends VerticalLayout implements BeforeEnterObserver {

    private final TariffService tariffService;
    private final Grid<Tariff> grid = new Grid<>(Tariff.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<HouseType> houseFilter = new ComboBox<>();
    private final ComboBox<TariffTier> tierFilter = new ComboBox<>();
    private final Span footerCount = new Span();

    public TariffsView(TariffService tariffService) {
        this.tariffService = tariffService;

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.STRETCH);
        getStyle().set("background", "#f1f5f9").set("box-sizing", "border-box");

        H2 title = new H2("Tariffs");
        title.getStyle().set("margin", "0").set("color", "#0f172a").set("font-size", "1.5rem");

        Paragraph subtitle = new Paragraph("Water pricing by house type and billing method (metered per gallon vs estimated flat fee)");
        subtitle.getStyle().set("margin", "0.25rem 0 0 0").set("color", "#64748b").set("font-size", "0.9rem");

        Button addTariff = new Button("Add tariff", VaadinIcon.PLUS.create(), e -> openTariffDialog(null));
        addTariff.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTariff.getStyle()
                .set("--lumo-primary-color", "#0f172a")
                .set("--lumo-primary-text-color", "#ffffff");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.END);
        VerticalLayout titles = new VerticalLayout(title, subtitle);
        titles.setPadding(false);
        titles.setSpacing(false);
        header.add(titles, addTariff);

        searchField.setPlaceholder("Search by name, description, rate, or fixed amount…");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());

        houseFilter.setPlaceholder("All house types");
        houseFilter.setItems(HouseType.values());
        houseFilter.setItemLabelGenerator(TariffsView::houseTypeLabel);
        houseFilter.setClearButtonVisible(true);
        houseFilter.setWidth("220px");
        houseFilter.addValueChangeListener(e -> refreshGrid());

        tierFilter.setPlaceholder("All tiers");
        tierFilter.setItems(TariffTier.values());
        tierFilter.setItemLabelGenerator(TariffsView::tierLabel);
        tierFilter.setClearButtonVisible(true);
        tierFilter.setWidth("180px");
        tierFilter.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, houseFilter, tierFilter);
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
        grid.addColumn(Tariff::getName).setHeader("Name").setFlexGrow(1);
        grid.addColumn(t -> houseTypeLabel(t.getHouseType())).setHeader("House type").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(t -> tierLabel(t.getTariffTier())).setHeader("Tier").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(TariffsView::formatAmountColumn).setHeader("Amount").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::activeBadge).setHeader("Status").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::editButton).setHeader("").setAutoWidth(true).setFlexGrow(0);
        grid.setAllRowsVisible(true);
    }

    private Button editButton(Tariff t) {
        Button edit = new Button(VaadinIcon.EDIT.create(), e -> openTariffDialog(t.getId()));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        edit.setAriaLabel("Edit tariff");
        return edit;
    }

    private Span activeBadge(Tariff t) {
        Span pill = new Span(t.isActive() ? "active" : "inactive");
        pill.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.2rem 0.65rem")
                .set("border-radius", "999px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("text-transform", "capitalize");
        if (t.isActive()) {
            pill.getStyle().set("background", "#0f172a").set("color", "white");
        } else {
            pill.getStyle().set("background", "#e2e8f0").set("color", "#475569");
        }
        return pill;
    }

    private void refreshGrid() {
        String q = searchField.getValue() != null ? searchField.getValue() : "";
        HouseType house = houseFilter.getValue();
        TariffTier tier = tierFilter.getValue();
        List<Tariff> rows = tariffService.listFiltered(q, house, tier);
        grid.setItems(rows);
        long total = tariffService.countAll();
        footerCount.setText("Showing " + rows.size() + " of " + total + " tariffs");
        footerCount.getStyle().set("font-size", "0.8rem").set("color", "#94a3b8").set("margin-top", "0.25rem");
    }

    private void openTariffDialog(Long existingId) {
        final boolean isEdit = existingId != null;
        Tariff loaded = null;
        if (isEdit) {
            loaded = tariffService.findById(existingId).orElse(null);
            if (loaded == null) {
                Notification.show("Tariff not found.", 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEdit ? "Edit tariff" : "Add tariff");

        TextField name = new TextField("Name");
        name.setRequired(true);
        name.setWidthFull();
        if (loaded != null) {
            name.setValue(loaded.getName());
        }

        TextArea description = new TextArea("Description");
        description.setWidthFull();
        if (loaded != null && loaded.getDescription() != null) {
            description.setValue(loaded.getDescription());
        }

        ComboBox<HouseType> house = new ComboBox<>("House type");
        house.setItems(HouseType.values());
        house.setRequired(true);
        house.setWidthFull();
        house.setItemLabelGenerator(TariffsView::houseTypeLabel);
        if (loaded != null) {
            house.setValue(loaded.getHouseType());
        }

        ComboBox<TariffTier> tier = new ComboBox<>("Billing tier");
        tier.setItems(TariffTier.values());
        tier.setRequired(true);
        tier.setWidthFull();
        tier.setItemLabelGenerator(TariffsView::tierLabel);
        tier.setHelperText("Meter: rate per gallon from meter reads. Estimated: one flat amount per bill.");
        if (loaded != null) {
            tier.setValue(loaded.getTariffTier());
        } else {
            tier.setValue(TariffTier.METER);
        }

        BigDecimalField ratePerUnit = new BigDecimalField("Rate per gallon");
        ratePerUnit.setWidthFull();
        ratePerUnit.setHelperText("Charge per gallon of water (from metered consumption).");
        if (loaded != null && loaded.getRatePerUnit() != null) {
            ratePerUnit.setValue(loaded.getRatePerUnit());
        } else {
            ratePerUnit.setValue(BigDecimal.ZERO);
        }

        BigDecimalField fixedTariff = new BigDecimalField("Fixed tariff amount");
        fixedTariff.setWidthFull();
        fixedTariff.setHelperText("Flat charge per billing period (estimated / non-metered).");
        if (loaded != null && loaded.getFixedTariff() != null) {
            fixedTariff.setValue(loaded.getFixedTariff());
        } else {
            fixedTariff.setValue(BigDecimal.ZERO);
        }

        Runnable syncTierFields = () -> applyTierFields(tier.getValue(), ratePerUnit, fixedTariff);
        syncTierFields.run();
        tier.addValueChangeListener(e -> syncTierFields.run());

        Checkbox active = new Checkbox("Active (available for new invoices)");
        active.setWidthFull();
        if (loaded != null) {
            active.setValue(loaded.isActive());
        } else {
            active.setValue(true);
        }

        VerticalLayout form = new VerticalLayout(name, description, house, tier, ratePerUnit, fixedTariff, active);
        form.setPadding(false);
        dialog.add(form);

        Button save = new Button("Save", e -> {
            if (name.getValue() == null || name.getValue().isBlank()
                    || house.getValue() == null
                    || tier.getValue() == null) {
                Notification.show("Please fill all required fields.", 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            TariffTier tt = tier.getValue();
            BigDecimal rpu = BigDecimal.ZERO;
            BigDecimal fix = BigDecimal.ZERO;
            if (tt == TariffTier.METER) {
                if (ratePerUnit.getValue() == null) {
                    Notification.show("Enter a rate per gallon for metered tariffs.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (ratePerUnit.getValue().compareTo(BigDecimal.ZERO) < 0) {
                    Notification.show("Rate per gallon cannot be negative.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                rpu = ratePerUnit.getValue();
            } else {
                if (fixedTariff.getValue() == null) {
                    Notification.show("Enter a fixed tariff amount for estimated billing.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (fixedTariff.getValue().compareTo(BigDecimal.ZERO) < 0) {
                    Notification.show("Fixed tariff cannot be negative.", 4000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                fix = fixedTariff.getValue();
            }

            Tariff draft = Tariff.builder()
                    .name(name.getValue().trim())
                    .description(description.getValue() != null && !description.getValue().isBlank()
                            ? description.getValue().trim()
                            : null)
                    .houseType(house.getValue())
                    .ratePerUnit(rpu)
                    .fixedTariff(fix)
                    .tariffTier(tt)
                    .isActive(active.getValue())
                    .build();
            if (!isEdit) {
                draft.setCreatedBy(SecuritySession.getUser());
            }

            try {
                if (isEdit) {
                    tariffService.update(existingId, draft);
                } else {
                    tariffService.create(draft);
                }
                dialog.close();
                refreshGrid();
                Notification.show(isEdit ? "Tariff updated." : "Tariff created.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private static void applyTierFields(TariffTier value, BigDecimalField ratePerUnit, BigDecimalField fixedTariff) {
        boolean meter = value == TariffTier.METER;
        ratePerUnit.setVisible(meter);
        fixedTariff.setVisible(!meter);
    }

    private static String formatAmountColumn(Tariff t) {
        if (t.getTariffTier() == TariffTier.ESTIMATED) {
            if (t.getFixedTariff() == null) {
                return "—";
            }
            return t.getFixedTariff().toPlainString() + " (fixed)";
        }
        if (t.getRatePerUnit() == null) {
            return "—";
        }
        return t.getRatePerUnit().toPlainString() + " / gal";
    }

    private static String houseTypeLabel(HouseType h) {
        if (h == null) {
            return "";
        }
        return h.name().replace('_', ' ');
    }

    private static String tierLabel(TariffTier t) {
        if (t == null) {
            return "";
        }
        return switch (t) {
            case METER -> "Meter";
            case ESTIMATED -> "Estimated";
        };
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
