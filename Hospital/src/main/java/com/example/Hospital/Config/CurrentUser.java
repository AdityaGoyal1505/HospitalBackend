package com.example.Hospital.Config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Reads who is calling from the login token.
 * The token's subject is the user id, and the "role" claim becomes ROLE_<name>.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    private static Authentication authOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    // The logged-in user's id. Throws 403 if nobody is logged in or the token has no valid id.
    public static Long id() {
        Long id = idOrNull();
        if (id == null) {
            throw new AccessDeniedException("Login required");
        }
        return id;
    }

    // The logged-in user's id, or null for a visitor (used by public endpoints).
    public static Long idOrNull() {
        Authentication auth = authOrNull();
        if (auth == null) {
            return null;
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean hasRole(String role) {
        Authentication auth = authOrNull();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    // Reception or admin: the people who manage patient data.
    public static boolean isStaff() {
        return hasRole("ADMIN") || hasRole("RECEPTION");
    }
}