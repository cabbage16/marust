package com.bamdoliro.maru.domain.form.exception;

import com.bamdoliro.maru.domain.form.exception.error.FormErrorProperty;
import com.bamdoliro.maru.shared.error.MaruException;

public class OutOfApplicationPeriodFormException extends MaruException {
    public OutOfApplicationPeriodFormException() {
        super(FormErrorProperty.OUT_OF_APPLICATION_FORM_PERIOD);
    }
}
