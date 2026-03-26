package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
/**
 * Admin shell (drawer + content). Not a standalone route: each admin view uses
 * {@code @Route(value = "admin/...", layout = AdminLayout.class)} so the route
 * registry stays consistent (a {@code @Route} on this class plus nested {@code layout =}
 * can break servlet startup).
 */
public class AdminLayout extends AppLayout implements BeforeEnterObserver {

    private final SideNav sideNav;

    public AdminLayout() {
        setPrimarySection(Section.DRAWER);
        setDrawerOpened(true);

        Div brand = buildBrand();
        sideNav = buildSideNav();
        Div footer = buildFooter();

        VerticalLayout drawer = new VerticalLayout(brand, sideNav, footer);
        drawer.setPadding(false);
        drawer.setSpacing(false);
        drawer.setSizeFull();
        drawer.setFlexGrow(1, sideNav);
        drawer.setAlignItems(FlexComponent.Alignment.STRETCH);
        drawer.getStyle()
                .set("background", "linear-gradient(180deg, #0c2748 0%, #0f3460 100%)")
                .set("padding", "1.25rem 0.75rem 1rem 0.75rem")
                .set("box-sizing", "border-box");

        addToDrawer(drawer);

        DrawerToggle toggle = new DrawerToggle();
        toggle.getStyle().set("color", "#0f172a");

        Span barTitle = new Span("Water Utility Admin");
        barTitle.getStyle()
                .set("font-weight", "700")
                .set("font-size", "1rem")
                .set("color", "#0f172a");

        addToNavbar(toggle, barTitle);

        getStyle().set("--drawer-width", "260px");
    }

    private static Div buildBrand() {
        Div drop = new Div();
        drop.getElement().setProperty("innerHTML",
                "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' width='22' height='22' fill='white' aria-hidden='true'>"
                        + "<path d='M12 2C12 2 4 11.5 4 16a8 8 0 0 0 16 0C20 11.5 12 2 12 2z'/>"
                        + "</svg>");
        drop.getStyle()
                .set("background-color", "#3b82f6")
                .set("border-radius", "50%")
                .set("width", "40px")
                .set("height", "40px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("flex-shrink", "0");

        Span title = new Span("Water Utility");
        title.getStyle()
                .set("color", "white")
                .set("font-weight", "700")
                .set("font-size", "1.05rem")
                .set("display", "block");

        Span sub = new Span("Admin Portal");
        sub.getStyle()
                .set("color", "rgba(255,255,255,0.72)")
                .set("font-size", "0.78rem")
                .set("display", "block");

        Div textCol = new Div(title, sub);
        textCol.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "0.1rem");

        Div row = new Div(drop, textCol);
        row.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("padding", "0 0.5rem 1.25rem 0.5rem")
                .set("border-bottom", "1px solid rgba(255,255,255,0.12)");

        return row;
    }

    private SideNav buildSideNav() {
        SideNav nav = new SideNav();
        nav.getStyle()
                .set("background", "transparent")
                .set("padding-top", "0.75rem");

        nav.addItem(navItem("Dashboard", "admin/dashboard", VaadinIcon.DASHBOARD));
        nav.addItem(navItem("Customers", "admin/customers", VaadinIcon.USERS));
        nav.addItem(navItem("Admin Users", "admin/todo/users", VaadinIcon.SHIELD));
        nav.addItem(navItem("Meter Readings", "admin/meter-readings", VaadinIcon.BAR_CHART));
        nav.addItem(navItem("Tariffs", "admin/tariffs", VaadinIcon.MONEY));
        nav.addItem(navItem("Invoices", "admin/invoices", VaadinIcon.FILE_TEXT));
        nav.addItem(navItem("Payments", "admin/payments", VaadinIcon.CREDIT_CARD));
        nav.addItem(navItem("Delinquency", "admin/todo/delinquency", VaadinIcon.WARNING));

        return nav;
    }

    private static SideNavItem navItem(String label, String path, VaadinIcon icon) {
        SideNavItem item = new SideNavItem(label, path);
        item.setPrefixComponent(icon.create());
        styleNavItem(item);
        return item;
    }

    private static void styleNavItem(SideNavItem item) {
        item.getStyle()
                .set("--_item-text-color", "rgba(255,255,255,0.88)")
                .set("--_item-icon-color", "rgba(255,255,255,0.75)");
    }

    private Div buildFooter() {
        SystemUser user = SecuritySession.getUser();
        String name = user != null ? user.getUsername() : "Admin";

        Span signedIn = new Span("Signed in as " + name);
        signedIn.getStyle()
                .set("color", "rgba(147,197,253,0.95)")
                .set("font-size", "0.8rem")
                .set("display", "block")
                .set("margin-bottom", "0.65rem");

        Button signOut = new Button("Sign Out", VaadinIcon.SIGN_OUT.create(), e -> {
            SecuritySession.logout();
            UI.getCurrent().navigate(LoginView.class);
        });
        signOut.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        signOut.getStyle()
                .set("background", "white")
                .set("color", "#1e40af")
                .set("border-radius", "10px")
                .set("font-weight", "600")
                .set("width", "100%")
                .set("justify-content", "center");

        Div box = new Div(signedIn, signOut);
        box.getStyle()
                .set("padding", "1rem 0.5rem 0 0.5rem")
                .set("margin-top", "auto")
                .set("border-top", "1px solid rgba(255,255,255,0.12)");
        return box;
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
