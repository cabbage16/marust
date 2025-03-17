package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.auth.exception.AuthorityMismatchException;
import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.CannotUpdateNotRejectedFormException;
import com.bamdoliro.maru.domain.form.exception.FormNotFoundException;
import com.bamdoliro.maru.domain.form.exception.OutOfApplicationFormPeriodException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateFormUseCaseTest {

    @InjectMocks
    private UpdateFormUseCase updateFormUseCase;

    @Mock
    private FormFacade formFacade;

    @Mock
    private ScheduleProperties scheduleProperties;

    @Test
    void 원서를_수정한다() {
        // given
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.reject();
        User user = form.getUser();

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formFacade.getForm(form.getId())).willReturn(form);

        // when
        updateFormUseCase.execute(user, form.getId(), FormFixture.createUpdateFormRequest(FormType.MEISTER_TALENT));

        // then
        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formFacade, times(1)).getForm(form.getId());
        assertEquals(FormType.MEISTER_TALENT, form.getType());
    }

    @Test
    void 원서를_수정할_때_원서_접수_기간이_아니면_에러가_발생한다() {
        // given
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.reject();
        User user = UserFixture.createUser();

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().plusDays(1));


        // when and then
        assertThrows(OutOfApplicationFormPeriodException.class, () ->
                updateFormUseCase.execute(user, form.getId(), FormFixture.createUpdateFormRequest(FormType.MEISTER_TALENT)));

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, never()).getEnd();
        verify(formFacade, never()).getForm(form.getId());
    }

    @Test
    void 원서를_수정할_때_원서가_없으면_에러가_발생한다() {
        // given
        Long formId = 1L;
        User user = UserFixture.createUser();

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        willThrow(new FormNotFoundException()).given(formFacade).getForm(formId);

        // when and then
        assertThrows(FormNotFoundException.class, () ->
                updateFormUseCase.execute(user, formId, FormFixture.createUpdateFormRequest(FormType.MEISTER_TALENT)));

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formFacade, times(1)).getForm(formId);
    }

    @Test
    void 원서를_수정할_때_본인의_원서가_아니면_에러가_발생한다() {
        // given
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.reject();
        User otherUser = UserFixture.createUser();

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formFacade.getForm(form.getId())).willReturn(form);

        // when and then
        assertThrows(AuthorityMismatchException.class, () ->
                updateFormUseCase.execute(otherUser, form.getId(), FormFixture.createUpdateFormRequest(FormType.MEISTER_TALENT))
        );

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formFacade, times(1)).getForm(form.getId());
    }

    @Test
    void 원서를_수정할_때_반려된_원서가_아니면_에러가_발생한다() {
        // given
        Form form = FormFixture.createForm(FormType.REGULAR);
        User user = form.getUser();

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formFacade.getForm(form.getId())).willReturn(form);

        // when and then
        assertThrows(CannotUpdateNotRejectedFormException.class, () ->
                updateFormUseCase.execute(user, form.getId(), FormFixture.createUpdateFormRequest(FormType.MEISTER_TALENT))
        );

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formFacade, times(1)).getForm(form.getId());
    }
}