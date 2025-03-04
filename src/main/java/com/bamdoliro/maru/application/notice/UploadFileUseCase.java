package com.bamdoliro.maru.application.notice;

import com.bamdoliro.maru.infrastructure.s3.FileService;
import com.bamdoliro.maru.infrastructure.s3.constants.FolderConstant;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.exception.FileCountLimitExceededException;
import com.bamdoliro.maru.infrastructure.s3.validator.DefaultFileValidator;
import com.bamdoliro.maru.presentation.notice.dto.response.UploadFileResponse;
import com.bamdoliro.maru.shared.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.*;

@RequiredArgsConstructor
@UseCase
public class UploadFileUseCase {

    private final FileService fileService;

    public List<UploadFileResponse> execute(List<FileMetadata> metadataList) {
        validateFileCount(metadataList);

        return metadataList.stream()
                .map(metadata1 -> {
                    String fileName = UUID.randomUUID() + "_" + metadata1.getFileName();
                    return new UploadFileResponse(
                            fileService.getPresignedUrl(FolderConstant.NOTICE_FILE, fileName, metadata1, metadata2 ->
                                    DefaultFileValidator.validate(metadata2, Set.of(MediaType.ALL))),
                            fileName
                    );
                })
                .toList();
    }

    public void validateFileCount(List<FileMetadata> metadataList) {
        if (metadataList.isEmpty() || metadataList.size() > 3) {
            throw new FileCountLimitExceededException(3);
        }
    }
}