package com.bamdoliro.maru.application.notice;

import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.exception.FileCountLimitExceededException;
import com.bamdoliro.maru.infrastructure.s3.validator.FileValidator;
import com.bamdoliro.maru.shared.fixture.SharedFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadFileUseCaseTest {

    @InjectMocks
    private UploadFileUseCase uploadFileUseCase;

    @Mock
    private FileService fileService;

    @Test
    void 공지사항_파일을_업로드한다() {
        // given
        List<FileMetadata> metadataList = List.of(
                new FileMetadata(
                        "notice-file.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        10 * MB
                )
        );
        given(fileService.getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class))).willReturn(SharedFixture.createNoticeFileUrlResponse());

        // when
        uploadFileUseCase.execute(metadataList);

        // then
        verify(fileService, times(1)).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }

    @Test
    void 공지사항_파일을_4개_이상_업로드하면_에러가_발생한다() {
        // given
        List<FileMetadata> metadataList = Collections.nCopies(4, new FileMetadata(
                "notice-file.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                10 * MB
        ));

        assertThrows(FileCountLimitExceededException.class, () -> uploadFileUseCase.execute(metadataList));

        verify(fileService, never()).getPresignedUrl(any(String.class), any(String.class), any(FileMetadata.class), any(FileValidator.class));
    }
}
