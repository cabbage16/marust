package com.bamdoliro.maru.infrastructure.s3.validator;

import com.bamdoliro.maru.infrastructure.s3.exception.EmptyFileException;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;

@FunctionalInterface
public interface FileValidator {
    void customValidate(FileMetadata request);

    default void validate(FileMetadata request) {
        if (request.getFileName().isBlank() || request.getFileName().lastIndexOf(".") == -1 || request.getFileSize() <= 0) {
            throw new EmptyFileException();
        }

        customValidate(request);
    }
}