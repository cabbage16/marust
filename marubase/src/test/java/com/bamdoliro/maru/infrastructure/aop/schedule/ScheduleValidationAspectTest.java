package com.bamdoliro.maru.infrastructure.aop.schedule;

import com.bamdoliro.maru.domain.form.exception.OutOfApplicationFormPeriodException;
import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduleValidationAspectTest {

    @InjectMocks
    private ScheduleValidationAspect scheduleValidationAspect;

    @Mock
    private ScheduleProperties scheduleProperties;

    @Test
    void 날짜_및_시간_검증에_성공한다() {
        LocalDateTime now = LocalDateTime.now();
        given(scheduleProperties.getStart()).willReturn(now.minusDays(1));
        given(scheduleProperties.getEnd()).willReturn(now.plusDays(1));

        scheduleValidationAspect.validateApplicationFormPeriod();

        verify(scheduleProperties, times(1)).getStart();
        verify(scheduleProperties, times(1)).getEnd();
    }

    @Test
    void 날짜_및_시간_검증에_실패하면_에러가_발생한다() {
        LocalDateTime now = LocalDateTime.now();
        given(scheduleProperties.getStart()).willReturn(now.plusDays(1));

        assertThrows(OutOfApplicationFormPeriodException.class, () ->
                scheduleValidationAspect.validateApplicationFormPeriod()
        );

        verify(scheduleProperties, times(1)).getStart();
    }
}
