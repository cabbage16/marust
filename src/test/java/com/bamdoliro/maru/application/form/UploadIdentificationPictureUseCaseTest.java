package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.form.exception.OutOfApplicationFormPeriodException;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
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
import java.util.Optional;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadIdentificationPictureUseCaseTest {

    @InjectMocks
    private UploadIdentificationPictureUseCase uploadIdentificationPictureUseCase;

    @Mock
    private FileService fileService;

    @Mock
    private FormRepository formRepository;

    @Mock
    private ScheduleProperties scheduleProperties;

    @Test
    void 증명_사진을_업로드한다() {
        // given
        User user = UserFixture.createUser();
        FileMetadata metadata = new FileMetadata(
                "identification-picture.png",
                MediaType.IMAGE_PNG_VALUE,
                MB
        );

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formRepository.findByUser(user)).willReturn(Optional.empty());
        given(fileService.getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class))).willReturn(SharedFixture.createIdentificationPictureUrlResponse());

        // when
        uploadIdentificationPictureUseCase.execute(user, metadata);

        // then
        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formRepository, times(1)).findByUser(user);
        verify(fileService, times(1)).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }

    @Test
    void 원서를_최종_제출할_때_원서_접수_기간이_아니면_에러가_발생한다() {
        // given
        User user = UserFixture.createUser();
        FileMetadata metadata = new FileMetadata(
                "identification-picture.png",
                MediaType.IMAGE_PNG_VALUE,
                MB
        );

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().plusDays(1));

        // when and then
        assertThrows(OutOfApplicationFormPeriodException.class, () -> uploadIdentificationPictureUseCase.execute(user, metadata));

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, never()).getEnd();
        verify(formRepository, never()).findByUser(user);
        verify(fileService, never()).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }

    @Test
    void 반려_또는_미제출_상태에서만_증명사진을_업로드할_수_있다() {
        // given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.approve();
        FileMetadata metadata = new FileMetadata(
                "identification-picture.png",
                MediaType.IMAGE_PNG_VALUE,
                MB
        );

        given(scheduleProperties.getStart()).willReturn(LocalDateTime.now().minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(LocalDateTime.now().plusDays(6));
        given(formRepository.findByUser(user)).willReturn(Optional.of(form));

        // when and then
        assertThrows(InvalidFormStatusException.class, () -> uploadIdentificationPictureUseCase.execute(user, metadata));

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
        verify(formRepository, times(1)).findByUser(user);
        verify(fileService, never()).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }
}