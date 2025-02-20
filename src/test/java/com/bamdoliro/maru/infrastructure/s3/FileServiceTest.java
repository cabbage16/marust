package com.bamdoliro.maru.infrastructure.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bamdoliro.maru.infrastructure.s3.dto.request.FileMetadata;
import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import com.bamdoliro.maru.infrastructure.s3.exception.*;
import com.bamdoliro.maru.shared.config.properties.S3Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.bamdoliro.maru.shared.constants.FileConstant.MB;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Mock
    private AmazonS3Client amazonS3Client;

    @Test
    void Presigned_URL을_생성한다() throws Exception {
        // given
        String url = "https://bucket.s3.ap-northeast-2.amazonaws.com/random-uuid-image.png";
        FileMetadata fileMetadata = new FileMetadata(
                "image.png",
                MediaType.IMAGE_PNG,
                25099L
        );
        given(amazonS3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).willReturn(new URL(url));

        // when
        UrlResponse response = fileService.getPresignedUrl("folder", "uuid", fileMetadata, metadata -> {
            if (!(metadata.getMediaType().equals(MediaType.IMAGE_PNG) || metadata.getMediaType().equals(MediaType.IMAGE_JPEG))) {
                throw new MediaTypeMismatchException();
            }

            if (metadata.getFileSize() > 2 * MB) {
                throw new FileSizeLimitExceededException();
            }
        });

        // then
        verify(amazonS3Client, times(2)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }
}