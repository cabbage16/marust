package com.bamdoliro.maru.infrastructure.scheduler;

import com.bamdoliro.maru.application.form.SelectFirstPassUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FormScheduler {

    private final SelectFirstPassUseCase selectFirstPassUseCase;

    @Scheduled(cron = "${schedule.select-first-pass-cron}")
    public void selectFirstPass() {
        selectFirstPassUseCase.execute();
    }
}
