package com.bamdoliro.maru.shared.error;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
public abstract class MaruException extends RuntimeException {

    private final ErrorProperty errorProperty;

    public MaruException(ErrorProperty errorProperty, Object... args) {
        super(String.format(errorProperty.getMessage(), args));
        this.errorProperty = errorProperty;
    }

    public MaruException(ErrorProperty errorProperty) {
        super(errorProperty.getMessage());
        this.errorProperty = errorProperty;
    }
}
