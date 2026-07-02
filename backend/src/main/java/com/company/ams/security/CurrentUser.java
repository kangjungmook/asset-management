package com.company.ams.security;

import com.company.ams.common.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public AuthPrincipal get() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        if (!(principal instanceof AuthPrincipal authPrincipal)) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return authPrincipal;
    }
}
