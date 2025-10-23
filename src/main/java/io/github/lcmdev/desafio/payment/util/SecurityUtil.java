package io.github.lcmdev.desafio.payment.util;

import org.springframework.security.core.context.SecurityContextHolder;

import static java.util.Objects.isNull;

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

