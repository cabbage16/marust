package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.exception.OutOfAdmissionAndPledgePeriodException;
import com.bamdoliro.maru.domain.form.service.FormFacade;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.validator.FileValidator;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.SharedFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadAdmissionAndPledgeUseCaseTest {

    @InjectMocks
    private UploadAdmissionAndPledgeUseCase uploadAdmissionAndPledgeUseCase;

    @Mock
    private FileService fileService;

    @Mock
    private FormFacade formFacade;

    @Mock
    private ScheduleProperties scheduleProperties;

    @Test
    void 입학등록원_및_금연서약서를_업로드한다() {
        //given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        FileMetadata metadata = new FileMetadata(
                "admission-and-pledge.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                10 * MB
        );
        form.pass();

        given(scheduleProperties.getAdmissionAndPledgeStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getAdmissionAndPledgeEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formFacade.getForm(user)).willReturn(form);
        given(fileService.getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class))).willReturn(SharedFixture.createAdmissionAndPledgeUrlResponse());

        //when
        uploadAdmissionAndPledgeUseCase.execute(user, metadata);

        //then
        verify(scheduleProperties, times(1)).getAdmissionAndPledgeStart();
        verify(scheduleProperties, times(1)).getAdmissionAndPledgeEnd();
        verify(formFacade, times(1)).getForm(user);
        verify(fileService, times(1)).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }

    @Test
    void 입학등록원_및_금연서약서를_업로드할_때_제출_기간이_아니면_에러가_발생한다() {
        // given
        User user = UserFixture.createUser();
        FileMetadata metadata = new FileMetadata(
                "admission-and-pledge.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                10 * MB
        );

        given(scheduleProperties.getAdmissionAndPledgeStart()).willReturn(LocalDateTime.now().plusDays(1));

        // when and then
        assertThrows(OutOfAdmissionAndPledgePeriodException.class, () -> uploadAdmissionAndPledgeUseCase.execute(user, metadata));

        verify(scheduleProperties, times(1)).getAdmissionAndPledgeStart();
        verify(scheduleProperties, never()).getAdmissionAndPledgeEnd();
        verify(formFacade, never()).getForm(user);
        verify(fileService, never()).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }

    @Test
    void 최종합격자가_아닌_지원자가_입학등록원_및_금연서약서를_업로드하면_에러가_발생한다() {
        //given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        FileMetadata metadata = new FileMetadata(
                "admission-and-pledge.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                10 * MB
        );

        given(scheduleProperties.getAdmissionAndPledgeStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getAdmissionAndPledgeEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formFacade.getForm(user)).willReturn(form);

        //when
        assertThrows(InvalidFormStatusException.class, () -> uploadAdmissionAndPledgeUseCase.execute(user, metadata));

        //then
        verify(scheduleProperties, times(1)).getAdmissionAndPledgeStart();
        verify(scheduleProperties, times(1)).getAdmissionAndPledgeEnd();
        verify(formFacade, times(1)).getForm(user);
        verify(fileService, never()).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }
}
