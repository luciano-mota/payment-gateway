package io.github.lcmdev.desafio.payment.util;

import static java.util.Objects.isNull;

import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {
    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (isNull(auth)) {
            return null;
        }

        var principal = auth.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        try {
            return Long.parseLong(String.valueOf(principal));
        } catch (Exception ex) {
            return null;
        }
    }
}