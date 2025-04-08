package com.bamdoliro.maru.domain.form.exception;

import com.bamdoliro.maru.domain.form.exception.error.FormErrorProperty;
import com.bamdoliro.maru.shared.error.MaruException;

public class WrongScoreException extends MaruException {

    public WrongScoreException(String message) {
        super(FormErrorProperty.WRONG_SCORE, message);
    }
}
