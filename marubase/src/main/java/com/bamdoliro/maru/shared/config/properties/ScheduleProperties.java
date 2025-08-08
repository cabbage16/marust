package com.bamdoliro.maru.shared.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Getter
@Setter
@Configuration
@ConfigurationProperties("schedule")
public class ScheduleProperties {

    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime announcementOfFirstPass;
    private LocalDateTime announcementOfSecondPass;
    private LocalDateTime codingTest;
    private LocalDateTime ncs;
    private LocalDateTime depthInterview;
    private LocalDateTime physicalExamination;
    private LocalDateTime entranceRegistrationPeriodStart;
    private LocalDateTime entranceRegistrationPeriodEnd;
    private LocalDateTime meisterTalentEntranceTime;
    private LocalDateTime meisterTalentExclusionEntranceTime;
    private LocalDateTime admissionAndPledgeStart;
    private LocalDateTime admissionAndPledgeEnd;
    private String selectFirstPassCron;
}
