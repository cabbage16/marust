package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.form.domain.Form;
import com.bamdoliro.maru.domain.form.domain.type.FormType;
import com.bamdoliro.maru.domain.form.exception.InvalidFormStatusException;
import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.persistence.form.FormRepository;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.validator.FileValidator;
import com.bamdoliro.maru.shared.fixture.FormFixture;
import com.bamdoliro.maru.shared.fixture.SharedFixture;
import com.bamdoliro.maru.shared.fixture.UserFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.Optional;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UploadFormUseCaseTest {

    @InjectMocks
    private UploadFormUseCase uploadFormUseCase;

    @Mock
    private FileService fileService;

    @Mock
    private FormRepository formRepository;

    @Test
    void 원서_서류를_업로드한다() {
        // given
        User user = UserFixture.createUser();
        FileMetadata metadata = new FileMetadata(
                "form.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                10 * MB
        );

        given(fileService.getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class))).willReturn(SharedFixture.createFormUrlResponse());

        // when
        uploadFormUseCase.execute(user, metadata);

        // then
        verify(fileService, times(1)).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }

    @Test
    void 반려_또는_미제출_상태에서만_원서_서류를_업로드할_수_있다() {
        // given
        User user = UserFixture.createUser();
        Form form = FormFixture.createForm(FormType.REGULAR);
        form.approve();
        FileMetadata metadata = new FileMetadata(
                "form.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                MB
        );

        given(formRepository.findByUser(user)).willReturn(Optional.of(form));

        // when and then
        assertThrows(InvalidFormStatusException.class, () -> uploadFormUseCase.execute(user, metadata));

        verify(formRepository, times(1)).findByUser(user);
    }
}