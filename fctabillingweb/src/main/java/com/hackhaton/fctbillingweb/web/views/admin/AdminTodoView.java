package com.hackhaton.fctbillingweb.web.views.admin;

import com.hackhaton.fctbillingweb.web.security.SecuritySession;
import com.hackhaton.fctbillingweb.web.views.LoginView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;

@Route(value = "admin/todo/:segment", layout = AdminLayout.class)
public class AdminTodoView extends VerticalLayout implements BeforeEnterObserver {

    private final H2 heading = new H2();
    private final Paragraph body = new Paragraph("This section is not implemented yet.");

    public AdminTodoView() {
        setSizeFull();
        setPadding(true);
        getStyle().set("background", "#f1f5f9");
        body.getStyle().set("color", "#64748b");
        add(heading, body);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!SecuritySession.isLoggedIn()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!SecuritySession.isAdmin()) {
            event.forwardTo(com.hackhaton.fctbillingweb.web.views.DashboardView.class);
            return;
        }
        String segment = event.getRouteParameters().get("segment").orElse("page");
        heading.setText(titleFromSegment(segment));
        UI ui = event.getUI();
        if (ui != null) {
            ui.getPage().setTitle(heading.getText() + " – Water Utility Admin");
        }
    }

    private static String titleFromSegment(String segment) {
        if (segment == null || segment.isBlank()) {
            return "Admin";
        }
        String[] parts = segment.replace('-', ' ').split(" ");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) {
                sb.append(p.substring(1));
            }
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}
