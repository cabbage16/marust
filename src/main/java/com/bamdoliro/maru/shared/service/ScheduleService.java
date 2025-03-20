package com.bamdoliro.maru.shared.service;

import com.bamdoliro.maru.shared.config.properties.ScheduleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private final ScheduleProperties scheduleProperties;

    public int getAdmissionYear() {
        return scheduleProperties.getStart().plusYears(1L).getYear();
    }

    public static String toLocaleString(LocalDateTime datetime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E) HH:mm", Locale.KOREA);
        return formatter.format(datetime);
    }

    public static String toLocaleString(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter startTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREA);
        DateTimeFormatter endTimeFormatter = DateTimeFormatter.ofPattern(" ~ MM.dd (E)", Locale.KOREA);
        return startTimeFormatter.format(startTime) + endTimeFormatter.format(endTime);
    }
}
