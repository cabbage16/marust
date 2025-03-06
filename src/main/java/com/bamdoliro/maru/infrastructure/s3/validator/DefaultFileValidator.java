package com.bamdoliro.maru.infrastructure.s3.validator;

import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.exception.FileSizeLimitExceededException;
import com.bamdoliro.maru.infrastructure.s3.exception.InvalidMediaTypeException;
import com.bamdoliro.maru.infrastructure.s3.exception.MediaTypeMismatchException;
import org.springframework.http.MediaType;

import java.util.Set;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;

public class DefaultFileValidator {

    public static void validate(FileMetadata metadata, Set<MediaType> allowedTypes, int maxSize) {
        try {
            MediaType mediaType = MediaType.parseMediaType(metadata.getMediaType());
            if (allowedTypes.stream().noneMatch(allowedType -> allowedType.includes(mediaType))) {
                throw new MediaTypeMismatchException();
            }
        } catch (org.springframework.http.InvalidMediaTypeException e) {
            throw new InvalidMediaTypeException();
        }

        if (metadata.getFileSize() > maxSize * MB) {
            throw new FileSizeLimitExceededException(maxSize);
        }
    }

    public static void validate(FileMetadata metadata, Set<MediaType> allowedTypes) {
        try {
            MediaType mediaType = MediaType.parseMediaType(metadata.getMediaType());
            if (allowedTypes.stream().noneMatch(allowedType -> allowedType.includes(mediaType))) {
                throw new MediaTypeMismatchException();
            }
        } catch (org.springframework.http.InvalidMediaTypeException e) {
            throw new InvalidMediaTypeException();
        }
    }
}
