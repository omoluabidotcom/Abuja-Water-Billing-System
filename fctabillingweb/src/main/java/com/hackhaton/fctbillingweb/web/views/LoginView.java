package com.hackhaton.fctbillingweb.web.views;

import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.hackhaton.fctwaterbilling.exception.AuthException;
import com.hackhaton.fctwaterbilling.service.AuthService;
import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Water Utility Portal – Sign In")
@CssImport("./styles/login-view.css")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthService      authService;
    private final TextField        usernameField;
    private final PasswordField    passwordField;

    public LoginView(AuthService authService) {
        this.authService = authService;

        // ── Page background ──────────────────────────────────────────
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(false);
        getStyle()
                .set("background", "linear-gradient(135deg, #bdd6ee 0%, #e3f1fb 45%, #bdd6ee 100%)")
                .set("min-height", "100vh")
                .set("box-sizing", "border-box");

        // ── Water-drop icon in blue circle ────────────────────────────
        // Inline SVG — avoids dependency on VaadinIcon enum constants
        // that changed between Vaadin major releases.
        Div dropIcon = new Div();
        dropIcon.getElement().setProperty("innerHTML",
                "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' "
                + "width='38' height='38' fill='white' aria-hidden='true'>"
                + "<path d='M12 2C12 2 4 11.5 4 16a8 8 0 0 0 16 0C20 11.5 12 2 12 2z'/>"
                + "</svg>");
        dropIcon.getStyle()
                .set("display",         "flex")
                .set("align-items",     "center")
                .set("justify-content", "center");

        Div iconCircle = new Div(dropIcon);
        iconCircle.getStyle()
                .set("background-color",  "#2563eb")
                .set("border-radius",     "50%")
                .set("width",             "76px")
                .set("height",            "76px")
                .set("display",           "flex")
                .set("align-items",       "center")
                .set("justify-content",   "center")
                .set("margin-bottom",     "1rem")
                .set("box-shadow",        "0 4px 16px rgba(37,99,235,0.35)");

        // ── Page title / subtitle ─────────────────────────────────────
        H1 pageTitle = new H1("Water Utility Portal");
        pageTitle.getStyle()
                .set("color",       "#0f172a")
                .set("font-size",   "1.9rem")
                .set("font-weight", "700")
                .set("margin",      "0")
                .set("text-align",  "center")
                .set("line-height", "1.2");

        Paragraph pageSubtitle = new Paragraph("Billing Management System");
        pageSubtitle.getStyle()
                .set("color",      "#6b7280")
                .set("font-size",  "0.95rem")
                .set("margin",     "0.3rem 0 2rem 0")
                .set("text-align", "center");

        // ── White card ────────────────────────────────────────────────
        VerticalLayout card = new VerticalLayout();
        card.addClassName("login-card");
        card.setWidth("440px");
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius",    "18px")
                .set("padding",          "2rem 2rem 1.75rem 2rem")
                .set("box-shadow",       "0 6px 32px rgba(0,0,0,0.09)")
                .set("max-width",        "calc(100vw - 2rem)");

        // Card heading
        H2 cardTitle = new H2("Sign In");
        cardTitle.getStyle()
                .set("font-size",   "1.5rem")
                .set("font-weight", "700")
                .set("color",       "#0f172a")
                .set("margin",      "0 0 0.2rem 0");

        Paragraph cardSubtitle = new Paragraph("Access your account using your credentials");
        cardSubtitle.getStyle()
                .set("color",      "#6b7280")
                .set("font-size",  "0.875rem")
                .set("margin",     "0 0 1.5rem 0");

        // Username field
        usernameField = new TextField("Username");
        usernameField.setPlaceholder("Enter your username");
        usernameField.setWidthFull();
        usernameField.getStyle().set("margin-bottom", "0.6rem");

        // Password field
        passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.getStyle().set("margin-bottom", "1.25rem");

        // Sign-in button  (black — overridden via .sign-in-btn CSS class)
        Button signInButton = new Button("Sign In", e -> handleLogin());
        signInButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        signInButton.addClassName("sign-in-btn");
        signInButton.getElement().getStyle()
                .set("--lumo-primary-color",      "#0f172a")
                .set("--lumo-primary-text-color", "#ffffff");

        // ── Demo credentials box ──────────────────────────────────────
        Div demoBox = new Div();
        demoBox.getStyle()
                .set("background-color", "#eff6ff")
                .set("border-radius",    "10px")
                .set("padding",          "1rem")
                .set("width",            "100%")
                .set("box-sizing",       "border-box");

        // Note row
        Div noteRow = new Div();
        Span bulb = new Span("💡 ");
        Span noteText = new Span("New accounts are created by administrators");
        noteText.getStyle()
                .set("color",     "#9ca3af")
                .set("font-size", "0.8rem");
        noteRow.add(bulb, noteText);

        demoBox.add(noteRow);

        // ── Assemble card ─────────────────────────────────────────────
        card.add(cardTitle, cardSubtitle,
                 usernameField, passwordField,
                 signInButton,
                 demoBox);

        // ── Assemble page ─────────────────────────────────────────────
        add(iconCircle, pageTitle, pageSubtitle, card);
    }

    // ── Route guard — skip login page if already authenticated ───────
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (SecuritySession.isLoggedIn()) {
            if (SecuritySession.isAdmin()) {
                event.forwardTo("admin/dashboard");
            } else {
                event.forwardTo(DashboardView.class);
            }
        }
    }

    // ── Login handler ─────────────────────────────────────────────────
    private void handleLogin() {
        String username = usernameField.getValue().trim();
        String password = passwordField.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            notify("Please enter your username and password.",
                    NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            var user = authService.login(username, password);
            SecuritySession.setUser(user);
            getUI().ifPresent(ui -> {
                if (user.getRole() == UserRole.ADMIN) {
                    ui.navigate("admin/dashboard");
                } else {
                    ui.navigate(DashboardView.class);
                }
            });
        } catch (AuthException ex) {
            passwordField.clear();
            notify(ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void notify(String message, NotificationVariant variant) {
        Notification n = Notification.show(message, 3500,
                Notification.Position.TOP_CENTER);
        n.addThemeVariants(variant);
    }
}

