package com.bamdoliro.maru.infrastructure.aop.log;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormStatus;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.log.AdminLoginLog;
import com.bamdoliro.maru.domain.log.FormSubmitLog;
import com.bamdoliro.maru.domain.log.FormUpdateLog;
import com.bamdoliro.maru.domain.log.UpdatedField;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.domain.user.domain.type.Authority;
import com.bamdoliro.maru.domain.user.service.UserFacade;
import com.bamdoliro.maru.infrastructure.persistence.log.AdminLoginLogRepository;
import com.bamdoliro.maru.infrastructure.persistence.log.FormSubmitLogRepository;
import com.bamdoliro.maru.infrastructure.persistence.log.FormUpdateLogRepository;
import com.bamdoliro.maru.infrastructure.persistence.log.UpdatedFieldRepository;
import com.bamdoliro.maru.presentation.auth.dto.request.LogInRequest;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Hibernate;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RequiredArgsConstructor
@Aspect
@Component
public class LogAspect {

    private final Javers javers;
    private final FormFacade formFacade;
    private final UserFacade userFacade;
    private final EntityManager entityManager;
    private final AdminLoginLogRepository adminLoginLogRepository;
    private final FormSubmitLogRepository formSubmitLogRepository;
    private final FormUpdateLogRepository formUpdateLogRepository;
    private final UpdatedFieldRepository updatedFieldRepository;


    @AfterReturning(value = "execution(* com.bamdoliro.maru.application.auth.LogInUseCase.execute(..))")
    public void logAdminLoginSuccess(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String phoneNumber = ((LogInRequest) args[0]).getPhoneNumber();

        User user = userFacade.getUser(phoneNumber);

        if (user.getAuthority() == Authority.ADMIN) {
            HttpServletRequest request = getCurrentHttpRequest();

            String clientIp = getClientIp(request);
            String userAgent = getUserAgent(request);

            AdminLoginLog log = new AdminLoginLog(phoneNumber, clientIp, userAgent, user);
            adminLoginLogRepository.save(log);
        }
    }

    @AfterReturning(value = "execution(* com.bamdoliro.maru.application.form.SubmitFormUseCase.execute(..))")
    public void logSubmitFormSuccess(JoinPoint joinPoint) {
        generateFormSubmitLog(joinPoint, FormStatus.SUBMITTED);
    }

    @AfterReturning(value = "execution(* com.bamdoliro.maru.application.form.SubmitFinalFormUseCase.execute(..))")
    public void logSubmitFinalFormSuccess(JoinPoint joinPoint) {
        generateFormSubmitLog(joinPoint, FormStatus.FINAL_SUBMITTED);
    }

    @Around(value = "execution(* com.bamdoliro.maru.application.form.UpdateFormUseCase.execute(..))")
    public void logUpdateFormSuccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        User user = (User) args[0];
        Form originalForm = formFacade.getForm(user);
        Hibernate.initialize(originalForm.getGrade().getSubjectList().getValue());
        Hibernate.initialize(originalForm.getGrade().getCertificateList().getValue());
        entityManager.detach(originalForm);

        joinPoint.proceed();

        Form updatedForm = formFacade.getForm(user);

        HttpServletRequest request = getCurrentHttpRequest();

        String clientIp = getClientIp(request);
        String userAgent = getUserAgent(request);

        FormUpdateLog formUpdateLog = FormUpdateLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .clientIp(clientIp)
                .userAgent(userAgent)
                .user(user)
                .form(updatedForm)
                .build();
        formUpdateLogRepository.save(formUpdateLog);

        Diff diff = javers.compare(originalForm, updatedForm);

        diff.getChanges().forEach(change -> {
            if (change instanceof ValueChange valueChange) {
                String fieldName = valueChange.getPropertyName();
                String oldValue = valueChange.getLeft().toString();
                String newValue = valueChange.getRight().toString();
                UpdatedField updatedField = UpdatedField.builder()
                        .formUpdateLog(formUpdateLog)
                        .filedName(fieldName)
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .build();
                updatedFieldRepository.save(updatedField);
            }
        });
    }

    private HttpServletRequest getCurrentHttpRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("x-forwarded-for");

        if (clientIp == null) return request.getRemoteAddr();
        return clientIp.split(",")[0];
    }

    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null) return "Unknown";
        return userAgent;
    }

    private void generateFormSubmitLog(JoinPoint joinPoint, FormStatus status) {
        Object[] args = joinPoint.getArgs();

        User user = (User) args[0];
        Form form = formFacade.getForm(user);

        HttpServletRequest request = getCurrentHttpRequest();

        String clientIp = getClientIp(request);
        String userAgent = getUserAgent(request);

        FormSubmitLog formSubmitLog = FormSubmitLog.builder()
                .phoneNumber(user.getPhoneNumber())
                .clientIp(clientIp)
                .userAgent(userAgent)
                .user(user)
                .form(form)
                .status(status)
                .build();
        formSubmitLogRepository.save(formSubmitLog);
    }
}
