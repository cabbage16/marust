package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.SharedFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadAdmissionAndPledgeUseCaseTest {

    @InjectMocks
    private UploadAdmissionAndPledgeUseCase uploadAdmissionAndPledgeUseCase;

    @Mock
    private FileService fileService;

    @Mock
    private FormFacade formFacade;

    @Test
    void 입학등록원_및_금연서약서를_업로드한다() {
        //given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.pass();

        given(formFacade.getForm(user)).willReturn(form);
        given(fileService.getPresignedUrl(any(String.class), any(String.class))).willReturn(SharedFixture.createFormUrlResponse());

        //when
        uploadAdmissionAndPledgeUseCase.execute(user);

        //then
        verify(formFacade, times(1)).getForm(user);
        verify(fileService, times(1)).getPresignedUrl(any(String.class), any(String.class));
    }

    @Test
    void 최종합격자가_아닌_지원자가_입학등록원_및_금연서약서를_업로드하면_에러가_발생한다() {
        //given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);

        given(formFacade.getForm(user)).willReturn(form);

        //when
        assertThrows(InvalidFormStatusException.class, () -> uploadAdmissionAndPledgeUseCase.execute(user));

        //then
        verify(formFacade, times(1)).getForm(user);
        verify(fileService, never()).getPresignedUrl(any(String.class), any(String.class));
    }
}
