package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import com.bamdoliro.maru.infrastructure.s3.exception.FileSizeLimitExceededException;
import com.bamdoliro.maru.infrastructure.s3.exception.MediaTypeMismatchException;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;

@RequiredArgsConstructor
@UseCase
public class UploadFormUseCase {

    private final FileService fileService;

    public UrlResponse execute(User user, FileMetadata fileMetadata) {
        return fileService.getPresignedUrl(FolderConstant.FORM, user.getUuid().toString(), fileMetadata, metadata -> {
            if (!metadata.getMediaType().equals(MediaType.APPLICATION_PDF)) {
                throw new MediaTypeMismatchException();
            }

            if (metadata.getFileSize() > 20 * MB) {
                throw new FileSizeLimitExceededException();
            }
        });
    }
}
