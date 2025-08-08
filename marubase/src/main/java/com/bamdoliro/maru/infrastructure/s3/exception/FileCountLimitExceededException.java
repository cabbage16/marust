package com.bamdoliro.maru.infrastructure.s3.exception;

import com.bamdoliro.maru.infrastructure.s3.exception.error.S3ErrorProperty;
import com.bamdoliro.maru.shared.error.MaruException;

public class FileCountLimitExceededException extends MaruException {
    public FileCountLimitExceededException(int maxCount) {
        super(S3ErrorProperty.FILE_COUNT_LIMIT_EXCEEDED, maxCount);
    }
}
