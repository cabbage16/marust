package com.bamdoliro.maru.infrastructure.s3.validator;

import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.exception.FileSizeLimitExceededException;
import com.bamdoliro.maru.infrastructure.s3.exception.InvalidMediaTypeException;
import com.bamdoliro.maru.infrastructure.s3.exception.MediaTypeMismatchException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Set;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultFileValidatorTest {

    @Test
    void 파일_검증을_성공한다() {
        // given
        FileMetadata metadata = new FileMetadata(
                "file.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                10 * MB
        );

        // when
        DefaultFileValidator.validate(metadata, Set.of(MediaType.APPLICATION_PDF), 20);
    }

    @Test
    void 허용된_미디어_타입이_아니면_에러가_발생한다() {
        FileMetadata metadata = new FileMetadata(
                "virus.bat",
                MediaType.TEXT_PLAIN_VALUE,
                10 * MB
        );

        assertThrows(
                MediaTypeMismatchException.class,
                () -> DefaultFileValidator.validate(metadata, Set.of(MediaType.APPLICATION_PDF), 20)
        );
    }


    @Test
    void 유효하지_않은_미디어_타입이면_에러가_발생한다() {
        FileMetadata metadata = new FileMetadata(
                "strange_file.pdf",
                "invalidMediaType",
                MB
        );

        assertThrows(
                InvalidMediaTypeException.class,
                () -> DefaultFileValidator.validate(metadata, Set.of(MediaType.APPLICATION_PDF), 20)
        );
    }

    @Test
    void 파일이_최대_용량을_초과하면_에러가_발생한다() {
        FileMetadata metadata = new FileMetadata(
                "too_big_file.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                1024 * MB
        );

        assertThrows(
                FileSizeLimitExceededException.class,
                () -> DefaultFileValidator.validate(metadata, Set.of(MediaType.APPLICATION_PDF), 20)
        );
    }
}