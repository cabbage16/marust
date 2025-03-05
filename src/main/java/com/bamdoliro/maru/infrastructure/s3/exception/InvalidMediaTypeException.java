package com.bamdoliro.maru.infrastructure.s3.exception;

import com.bamdoliro.maru.infrastructure.s3.exception.error.S3ErrorProperty;
import com.bamdoliro.maru.shared.error.MaruException;

public class InvalidMediaTypeException extends MaruException {
    public InvalidMediaTypeException() {
        super(S3ErrorProperty.INVALID_MEDIA_TYPE);
    }
}
