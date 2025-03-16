package com.bamdoliro.maru.domain.form.exception;

import com.bamdoliro.maru.domain.form.exception.error.FormErrorProperty;
import com.bamdoliro.maru.shared.error.MaruException;

public class OutOfApplicationPeriodException extends MaruException {
    public OutOfApplicationPeriodException() {
        super(FormErrorProperty.OUT_OF_APPLICATION_PERIOD);
    }
}
