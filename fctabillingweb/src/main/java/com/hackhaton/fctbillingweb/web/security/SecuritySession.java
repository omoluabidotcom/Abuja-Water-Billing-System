package com.hackhaton.fctbillingweb.web.security;

import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.vaadin.flow.server.VaadinSession;

/**
 * Utility class that stores/retrieves the authenticated {@link SystemUser}
 * from the current {@link VaadinSession}.
 *
 * Must only be called from within an active Vaadin request/push thread.
 */
public final class SecuritySession {

    private static final String USER_KEY = "authenticated_user";

    private SecuritySession() {}

    /** Store the authenticated user in the current session. */
    public static void setUser(SystemUser user) {
        VaadinSession.getCurrent().setAttribute(USER_KEY, user);
    }

    /** Retrieve the authenticated user, or {@code null} if not logged in. */
    public static SystemUser getUser() {
        return (SystemUser) VaadinSession.getCurrent().getAttribute(USER_KEY);
    }

    /** Returns {@code true} when a user is stored in the current session. */
    public static boolean isLoggedIn() {
        VaadinSession session = VaadinSession.getCurrent();
        return session != null && session.getAttribute(USER_KEY) != null;
    }

    /** Returns {@code true} if the logged-in user has the ADMIN role. */
    public static boolean isAdmin() {
        SystemUser user = getUser();
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    /** Returns {@code true} if the logged-in user has the CUSTOMER role. */
    public static boolean isCustomer() {
        SystemUser user = getUser();
        return user != null && user.getRole() == UserRole.CUSTOMER;
    }

    /** Invalidate the session (logout). */
    public static void logout() {
        VaadinSession current = VaadinSession.getCurrent();
        if (current != null) {
            current.setAttribute(USER_KEY, null);
            current.getSession().invalidate();
        }
    }
}

