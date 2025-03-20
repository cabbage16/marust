package com.bamdoliro.maru.domain.form.exception;

import com.bamdoliro.maru.domain.form.exception.error.FormErrorProperty;
import com.bamdoliro.maru.shared.error.MaruException;

public class OutOfAdmissionAndPledgePeriodException extends MaruException {
    public OutOfAdmissionAndPledgePeriodException() {
        super(FormErrorProperty.OUT_OF_ADMISSION_AND_PLEDGE_PERIOD);
    }
}
