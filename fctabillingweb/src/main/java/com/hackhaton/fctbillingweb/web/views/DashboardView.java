package com.hackhaton.fctbillingweb.web.views;

import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;

@Route("dashboard")
@PageTitle("Dashboard – Water Utility Portal")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    // ── Route guard — must be logged in ──────────────────────────────
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!SecuritySession.isLoggedIn()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (SecuritySession.isAdmin()) {
            event.forwardTo("admin/dashboard");
        }
    }

    public DashboardView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f1f5f9");

        SystemUser user = SecuritySession.getUser();
        if (user == null) return; // guard (beforeEnter already handles redirect)

        // ── Top navigation bar ────────────────────────────────────────
        HorizontalLayout navbar = buildNavbar(user);

        // ── Main content ──────────────────────────────────────────────
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle()
                .set("max-width", "1100px")
                .set("width",     "100%")
                .set("margin",    "0 auto");

        // Welcome card
        Div welcomeCard = buildWelcomeCard(user);

        // Summary cards row
        HorizontalLayout cards = buildSummaryCards(user);

        content.add(welcomeCard, cards);
        add(navbar, content);
    }

    // ── Navbar ────────────────────────────────────────────────────────
    private HorizontalLayout buildNavbar(SystemUser user) {
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.setWidthFull();
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        navbar.getStyle()
                .set("background-color", "#0f172a")
                .set("padding",          "0 2rem")
                .set("height",           "64px")
                .set("flex-shrink",      "0");

        // Logo + app name
        HorizontalLayout brand = new HorizontalLayout();
        brand.setAlignItems(FlexComponent.Alignment.CENTER);
        brand.setSpacing(true);

        Div logoCircle = new Div();
        logoCircle.getElement().setProperty("innerHTML",
                "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' "
                + "width='24' height='24' fill='white'>"
                + "<path d='M12 2C12 2 4 11.5 4 16a8 8 0 0 0 16 0C20 11.5 12 2 12 2z'/>"
                + "</svg>");
        logoCircle.getStyle()
                .set("background-color", "#2563eb")
                .set("border-radius",    "50%")
                .set("width",            "36px")
                .set("height",           "36px")
                .set("display",          "flex")
                .set("align-items",      "center")
                .set("justify-content",  "center");

        Span appName = new Span("Water Utility Portal");
        appName.getStyle()
                .set("color",       "white")
                .set("font-weight", "700")
                .set("font-size",   "1rem");

        brand.add(logoCircle, appName);

        // User info + logout
        HorizontalLayout userArea = new HorizontalLayout();
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.setSpacing(true);

        Span usernameLabel = new Span(user.getUsername());
        usernameLabel.getStyle().set("color", "#94a3b8").set("font-size", "0.875rem");

        Span roleBadge = new Span(user.getRole().name());
        roleBadge.getStyle()
                .set("background-color", user.getRole() == UserRole.ADMIN ? "#1d4ed8" : "#059669")
                .set("color",        "white")
                .set("padding",      "0.15rem 0.6rem")
                .set("border-radius","999px")
                .set("font-size",    "0.75rem")
                .set("font-weight",  "600");

        Button logoutBtn = new Button("Logout", e -> handleLogout());
        logoutBtn.getStyle()
                .set("background-color", "transparent")
                .set("color",            "#94a3b8")
                .set("border",           "1px solid #334155")
                .set("border-radius",    "6px")
                .set("cursor",           "pointer")
                .set("font-size",        "0.85rem");

        userArea.add(usernameLabel, roleBadge, logoutBtn);
        navbar.add(brand, userArea);
        return navbar;
    }

    // ── Welcome card ──────────────────────────────────────────────────
    private Div buildWelcomeCard(SystemUser user) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius",    "14px")
                .set("padding",          "1.75rem 2rem")
                .set("box-shadow",       "0 2px 12px rgba(0,0,0,0.07)")
                .set("margin-bottom",    "1.25rem");

        H2 welcome = new H2("Welcome back, " + user.getUsername() + "! 👋");
        welcome.getStyle()
                .set("margin",      "0 0 0.5rem 0")
                .set("color",       "#0f172a")
                .set("font-size",   "1.4rem");

        String roleDesc = user.getRole() == UserRole.ADMIN
                ? "You are signed in as an Administrator."
                : "You are signed in as a Customer.";

        Paragraph sub = new Paragraph(roleDesc);
        sub.getStyle().set("color", "#6b7280").set("margin", "0 0 0.75rem 0").set("font-size", "0.9rem");

        String lastLogin = user.getLastLoginAt() != null
                ? "Last login: " + user.getLastLoginAt()
                        .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                : "This is your first login.";

        Paragraph lastLoginP = new Paragraph(lastLogin);
        lastLoginP.getStyle().set("color", "#9ca3af").set("margin", "0").set("font-size", "0.8rem");

        card.add(welcome, sub, lastLoginP);
        return card;
    }

    // ── Summary cards ─────────────────────────────────────────────────
    private HorizontalLayout buildSummaryCards(SystemUser user) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("flex-wrap", "wrap");

        if (user.getRole() == UserRole.ADMIN) {
            row.add(
                summaryCard("👤", "Users",          "Manage system users",     "#2563eb"),
                summaryCard("📄", "Invoices",        "View & generate invoices", "#7c3aed"),
                summaryCard("💧", "Meter Readings",  "Upload & manage readings", "#0891b2"),
                summaryCard("📊", "Reports",         "Revenue & usage reports",  "#059669")
            );
        } else {
            row.add(
                summaryCard("📄", "My Invoices",     "View your billing history", "#2563eb"),
                summaryCard("💧", "My Meter",        "Check meter readings",      "#0891b2"),
                summaryCard("💳", "Make Payment",    "Pay outstanding balance",   "#7c3aed"),
                summaryCard("📣", "Raise Dispute",   "Dispute an invoice",        "#dc2626")
            );
        }
        return row;
    }

    private Div summaryCard(String icon, String title, String desc, String accent) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius",    "12px")
                .set("padding",          "1.5rem")
                .set("box-shadow",       "0 2px 10px rgba(0,0,0,0.06)")
                .set("flex",             "1 1 200px")
                .set("min-width",        "160px")
                .set("cursor",           "pointer")
                .set("border-left",      "4px solid " + accent)
                .set("transition",       "box-shadow 0.2s");

        Span iconEl = new Span(icon);
        iconEl.getStyle().set("font-size", "1.75rem").set("display", "block").set("margin-bottom", "0.6rem");

        H3 titleEl = new H3(title);
        titleEl.getStyle()
                .set("margin",      "0 0 0.3rem 0")
                .set("font-size",   "1rem")
                .set("color",       "#0f172a")
                .set("font-weight", "600");

        Paragraph descEl = new Paragraph(desc);
        descEl.getStyle().set("margin", "0").set("font-size", "0.8rem").set("color", "#6b7280");

        card.add(iconEl, titleEl, descEl);
        return card;
    }

    // ── Logout ────────────────────────────────────────────────────────
    private void handleLogout() {
        SecuritySession.logout();
        UI.getCurrent().navigate(LoginView.class);
    }
}

