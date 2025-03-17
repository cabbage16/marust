package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.FormAlreadySubmittedException;
import com.bamdoliro.maru.domain.form.exception.OutOfApplicationFormPeriodException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitFinalFormUseCaseTest {

    @InjectMocks
    private SubmitFinalFormUseCase submitFinalFormUseCase;

    @Mock
    private FormFacade formFacade;

    @Mock
    private ScheduleProperties scheduleProperties;


    @Test
    void 원서를_최종_제출한다() {
        // given
        Form form = FormFixture.createForm(FormType.REGULAR);

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formFacade.getForm(any(User.class))).willReturn(form);

        // when
        submitFinalFormUseCase.execute(form.getUser());

        // then
        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formFacade, times(1)).getForm(any(User.class));
    }

    @Test
    void 원서를_최종_제출할_때_원서_접수_기간이_아니면_에러가_발생한다() {
        // given
        Form form = FormFixture.createForm(FormType.REGULAR);

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().plusDays(1));

        // when and then
        assertThrows(OutOfApplicationFormPeriodException.class, () -> submitFinalFormUseCase.execute(form.getUser()));

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, never()).getEnd();
        verify(formFacade, never()).getForm(any(User.class));
    }

    @Test
    void 원서를_최종_제출할_때_이미_제출했다면_에러가_발생한다() {
        // given
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.approve();

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formFacade.getForm(any(User.class))).willReturn(form);

        // when and then
        assertThrows(FormAlreadySubmittedException.class, () -> submitFinalFormUseCase.execute(form.getUser()));

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formFacade, times(1)).getForm(any(User.class));
    }
}