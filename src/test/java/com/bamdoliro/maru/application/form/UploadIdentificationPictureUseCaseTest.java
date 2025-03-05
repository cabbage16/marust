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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadIdentificationPictureUseCaseTest {

    @InjectMocks
    private UploadIdentificationPictureUseCase uploadIdentificationPictureUseCase;

    @Mock
    private FileService fileService;

    @Mock
    private FormRepository formRepository;

    @Test
    void 증명_사진을_업로드한다() {
        // given
        User user = UserFixture.createUser();
        FileMetadata metadata = new FileMetadata(
                "identification-picture.png",
                MediaType.IMAGE_PNG_VALUE,
                MB
        );

        given(formRepository.findByUser(user)).willReturn(Optional.empty());
        given(fileService.getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class))).willReturn(SharedFixture.createIdentificationPictureUrlResponse());

        // when
        uploadIdentificationPictureUseCase.execute(user, metadata);

        // then
        verify(formRepository, times(1)).findByUser(user);
        verify(fileService, times(1)).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
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

        given(formRepository.findByUser(user)).willReturn(Optional.of(form));

        // when and then
        assertThrows(InvalidFormStatusException.class, () -> uploadIdentificationPictureUseCase.execute(user, metadata));

        verify(formRepository, times(1)).findByUser(user);
        verify(fileService, never()).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }
}