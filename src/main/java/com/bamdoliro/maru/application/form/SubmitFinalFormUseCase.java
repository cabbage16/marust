package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.exception.FormAlreadySubmittedException;
import com.bamdoliro.maru.domain.form.exception.OutOfApplicationFormPeriodException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.shared.annotation.UseCase;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@UseCase
public class SubmitFinalFormUseCase {

    private final FormFacade formFacade;
    private final ScheduleProperties scheduleProperties;

    @Transactional
    public void execute(User user) {
        validateApplicationPeriod(LocalDateTime.now());
        Form form = formFacade.getForm(user);
        validateFormStatus(form);

        form.submit();
    }

    private void validateApplicationPeriod(LocalDateTime now) {
        if (now.isBefore(scheduleProperties.getStart()) || now.isAfter(scheduleProperties.getEnd())) {
            throw new OutOfApplicationFormPeriodException();
        }
    }

    private void validateFormStatus(Form form) {
        if (!(form.isSubmitted() || form.isRejected())) {
            throw new FormAlreadySubmittedException();
        }
    }
}
