package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;

@Route(value = "admin/dashboard", layout = AdminLayout.class)
@PageTitle("Dashboard – Water Utility Admin")
public class AdminDashboardView extends VerticalLayout implements BeforeEnterObserver {

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

    public AdminDashboardView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background", "#f1f5f9")
                .set("box-sizing", "border-box");

        SystemUser user = SecuritySession.getUser();
        if (user == null) {
            return;
        }

        Div welcomeCard = buildWelcomeCard(user);
        HorizontalLayout cards = buildSummaryCards();

        add(welcomeCard, cards);
    }

    private Div buildWelcomeCard(SystemUser user) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "14px")
                .set("padding", "1.75rem 2rem")
                .set("box-shadow", "0 2px 12px rgba(0,0,0,0.07)")
                .set("margin-bottom", "1.25rem");

        H2 welcome = new H2("Welcome back, " + user.getUsername() + "!");
        welcome.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#0f172a")
                .set("font-size", "1.4rem");

        Paragraph sub = new Paragraph("You are signed in as an Administrator.");
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

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("flex-wrap", "wrap");

        row.add(
                summaryCard("Users", "Manage system users", "#2563eb"),
                summaryCard("Invoices", "View & generate invoices", "#7c3aed"),
                summaryCard("Meter Readings", "Upload & manage readings", "#0891b2"),
                summaryCard("Reports", "Revenue & usage reports", "#059669")
        );
        return row;
    }

    private Div summaryCard(String title, String desc, String accent) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.06)")
                .set("flex", "1 1 200px")
                .set("min-width", "160px")
                .set("cursor", "default")
                .set("border-left", "4px solid " + accent);

        H3 titleEl = new H3(title);
        titleEl.getStyle()
                .set("margin", "0 0 0.3rem 0")
                .set("font-size", "1rem")
                .set("color", "#0f172a")
                .set("font-weight", "600");

        Paragraph descEl = new Paragraph(desc);
        descEl.getStyle().set("margin", "0").set("font-size", "0.8rem").set("color", "#6b7280");

        card.add(titleEl, descEl);
        return card;
    }
}
