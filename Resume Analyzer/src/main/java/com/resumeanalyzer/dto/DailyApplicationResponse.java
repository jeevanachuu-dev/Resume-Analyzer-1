package com.resumeanalyzer.dto;

import java.time.LocalDate;

public record DailyApplicationResponse(
        LocalDate date,
        long count
) {
}
