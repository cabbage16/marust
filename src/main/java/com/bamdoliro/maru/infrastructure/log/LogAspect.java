package com.bamdoliro.maru.infrastructure.log;

import com.bamdoliro.maru.domain.log.AdminLoginLog;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.domain.user.domain.type.Authority;
import com.bamdoliro.maru.domain.user.service.UserFacade;
import com.bamdoliro.maru.infrastructure.persistence.log.AdminLoginLogRepository;
import com.bamdoliro.maru.presentation.auth.dto.request.LogInRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RequiredArgsConstructor
@Aspect
@Component
public class LogAspect {

    private final AdminLoginLogRepository adminLoginLogRepository;
    private final UserFacade userFacade;

    @AfterReturning(value = "execution(* com.bamdoliro.maru.application.auth.LogInUseCase.execute(..))")
    public void logAdminLoginSuccess(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String phoneNumber = ((LogInRequest) args[0]).getPhoneNumber();

        User user = userFacade.getUser(phoneNumber);

        if (user.getAuthority() == Authority.ADMIN) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String clientIp = request.getHeader("X-Forwarded-For");

            if (clientIp != null)
                clientIp = clientIp.split(",")[0];
            else
                clientIp = request.getRemoteAddr();

            String userAgent = request.getHeader("User-Agent");

            if (userAgent == null)
                userAgent = "Unknown";

            AdminLoginLog log = new AdminLoginLog(phoneNumber, clientIp, userAgent, user);
            adminLoginLogRepository.save(log);
        }
    }
}
