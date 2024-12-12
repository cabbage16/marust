package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormStatus;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EnterFormUseCaseTest {

    @InjectMocks
    private EnterFormUseCase enterFormUseCase;

    @Mock
    private FormFacade formFacade;

    @Test
    void 최종합격자가_입학등록원_및_금연서약서를_제출한다() {
        // given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.pass();
        given(formFacade.getForm(user)).willReturn(form);

        // when
        enterFormUseCase.execute(user);

        // then
        verify(formFacade, times(1)).getForm(user);
        assertEquals(form.getStatus(), FormStatus.ENTERED);
    }

    @Test
    void 입학등록원_및_금연서약서를_제출한_지원자가_재제출한다() {
        // given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.enter();
        given(formFacade.getForm(user)).willReturn(form);

        // when
        enterFormUseCase.execute(user);

        // then
        verify(formFacade, times(1)).getForm(user);
        assertEquals(form.getStatus(), FormStatus.ENTERED);
    }

    @Test
    void 입학등록원을_제출했거나_최종합격한_지원자가_아닌_지원자가_입학등록원_및_금연서약서를_제출하면_에러가_발생한다() {
        //given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        given(formFacade.getForm(user)).willReturn(form);

        //when and then
        assertThrows(InvalidFormStatusException.class, () -> enterFormUseCase.execute(user));
        verify(formFacade, times(1)).getForm(user);
    }
}
