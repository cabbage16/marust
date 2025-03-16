package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.exception.CannotUpdateNotRejectedFormException;
import com.bamdoliro.maru.domain.form.exception.OutOfApplicationFormPeriodException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.presentation.form.dto.request.UpdateFormRequest;
import com.bamdoliro.maru.shared.annotation.UseCase;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@UseCase
public class UpdateFormUseCase {

    private final FormFacade formFacade;
    private final ScheduleProperties scheduleProperties;

    @Transactional
    public void execute(User user, Long id, UpdateFormRequest request) {
        validateApplicationPeriod(LocalDateTime.now());
        Form form = formFacade.getForm(id);
        form.isApplicant(user);
        validateFormStatus(form);

        form.update(
                request.getApplicant().toValue(),
                request.getParent().toValue(),
                request.getEducation().toValue(),
                request.getGrade().toValue(),
                request.getDocument().toValue(),
                request.getType()
        );
    }

    private void validateFormStatus(Form form) {
        if (!form.isRejected()) {
            throw new CannotUpdateNotRejectedFormException();
        }
    }

    private void validateApplicationPeriod(LocalDateTime now) {
        if (now.isBefore(scheduleProperties.getStart()) || now.isAfter(scheduleProperties.getEnd())) {
            throw new OutOfApplicationFormPeriodException();
        }
    }
}
