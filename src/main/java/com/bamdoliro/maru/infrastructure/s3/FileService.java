package com.bamdoliro.maru.infrastructure.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.*;
import com.bamdoliro.maru.infrastructure.s3.dto.response.UrlResponse;
import com.bamdoliro.maru.infrastructure.s3.exception.FailedToSaveException;
import com.bamdoliro.maru.infrastructure.s3.validator.FileValidator;
import com.bamdoliro.maru.shared.config.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Properties s3Properties;
    private final AmazonS3Client amazonS3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Deprecated
    public UrlResponse execute(MultipartFile file, String folder, String fileName, FileValidator validator) {
        validator.validate(file);
        String fullFileName = createFileName(folder, fileName);

        try {
            PutObjectRequest request = new PutObjectRequest(
                    s3Properties.getBucket(),
                    fullFileName,
                    file.getInputStream(),
                    getObjectMetadata(file)
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3Client.putObject(request);
        } catch (SdkClientException | IOException e) {
            throw new FailedToSaveException();
        }

        return new UrlResponse(
                amazonS3Client.getUrl(s3Properties.getBucket(), fullFileName).toString(),
                amazonS3Client.getUrl(s3Properties.getBucket(), fullFileName).toString()
        );
    }

    public String getUploadPresignedUrl(String folder, String fileName) {
        String fullFileName = createFileName(folder, fileName);
        GeneratePresignedUrlRequest request = getGenerateUploadPresignedUrlRequest(bucket, fullFileName);

        return amazonS3Client.generatePresignedUrl(request).toString();
    }

    public String getDownloadPresignedUrl(String folder, String fileName) {
        String fullFileName = createFileName(folder, fileName);
        GeneratePresignedUrlRequest request = getGenerateDownloadPresignedUrlRequest(bucket, fullFileName);

        return request != null ? amazonS3Client.generatePresignedUrl(request).toString() : null;
    }

    public UrlResponse getPresignedUrl(String folder, String fileName) {
        return new UrlResponse(
                getUploadPresignedUrl(folder, fileName),
                getDownloadPresignedUrl(folder, fileName)
        );
    }

    private GeneratePresignedUrlRequest getGenerateUploadPresignedUrlRequest(String bucket, String fileName) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, fileName)
                .withMethod(HttpMethod.PUT)
                .withExpiration(getPresignedUrlExpiration(3));

        request.putCustomRequestHeader(Headers.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        return request;
    }

    private GeneratePresignedUrlRequest getGenerateDownloadPresignedUrlRequest(String bucket, String fileName) {
        try {
            amazonS3Client.getObjectMetadata(bucket, fileName);

            return new GeneratePresignedUrlRequest(bucket, fileName)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(getPresignedUrlExpiration(60 * 10));
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
            throw e;
        }
    }

    private Date getPresignedUrlExpiration(int duration) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * duration;
        expiration.setTime(expTimeMillis);

        return expiration;
    }

    private String createFileName(String folder, String fileName) {
        return folder + "/" + fileName;
    }

    private ObjectMetadata getObjectMetadata(MultipartFile file) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());
        return objectMetadata;
    }
}