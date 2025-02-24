package com.bamdoliro.maru.application.form;

import com.bamdoliro.maru.domain.user.domain.User;
import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import com.bamdoliro.maru.infrastructure.s3.validator.DefaultFileValidator;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Set;

@RequiredArgsConstructor
@UseCase
public class UploadFormUseCase {

    private final FileService fileService;

    public UrlResponse execute(User user, FileMetadata fileMetadata) {
        return fileService.getPresignedUrl(FolderConstant.FORM, user.getUuid().toString(), fileMetadata, metadata ->
                DefaultFileValidator.validate(metadata, Set.of(MediaType.APPLICATION_PDF), 20)
        );
    }
}
