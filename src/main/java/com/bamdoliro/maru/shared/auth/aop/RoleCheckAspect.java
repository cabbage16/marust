package com.bamdoliro.maru.shared.auth.aop;

import com.bamdoliro.maru.shared.auth.Authority;
import com.bamdoliro.maru.shared.auth.annotation.RoleCheck;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class RoleCheckAspect {

    @Before("@annotation(roleCheck)")
    public void checkRole(RoleCheck roleCheck) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        List<String> userRoles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        List<String> requiredRoles = Authority.getRoles(roleCheck.value());

        boolean hasPermission = requiredRoles.stream()
                .anyMatch(userRoles::contains);

        if (!hasPermission) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
    }
}
