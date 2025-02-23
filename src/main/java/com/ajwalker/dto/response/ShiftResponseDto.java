package com.ajwalker.dto.response;

import com.ajwalker.entity.Shift;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShiftResponseDto(
        Long shiftId,
        String shiftName,
        LocalTime beginTime,
        LocalTime endTime
) {
}
