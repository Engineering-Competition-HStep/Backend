package com.Hstep.Hstep.domain.notice.dto;

import com.Hstep.Hstep.domain.notice.entity.NoticeSource;

import java.time.LocalDate;

public record NoticeSearchCondition(
        String keyword,
        String category,
        NoticeSource source,
        LocalDate startDate,
        LocalDate endDate
) {
}
