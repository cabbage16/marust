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
                .map(metadata -> {
                    String fileName = UUID.randomUUID() + "_" + metadata.getFileName();
                    return new UploadFileResponse(
                            fileService.getPresignedUrl(FolderConstant.NOTICE_FILE, fileName, metadata, metadata1 -> {
                                MediaType image = MediaType.parseMediaType("image/*");
                                MediaType docx = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                                MediaType pptx = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
                                MediaType xlsx = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                                DefaultFileValidator.validate(metadata1, Set.of(
                                        image,
                                        MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_PDF,
                                        docx, pptx, xlsx
                                ));
                            }),
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