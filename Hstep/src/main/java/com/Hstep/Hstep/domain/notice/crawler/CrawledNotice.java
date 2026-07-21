package com.Hstep.Hstep.domain.notice.crawler;

import com.Hstep.Hstep.domain.notice.entity.NoticeSource;

import java.time.LocalDateTime;

public record CrawledNotice(
        String title,
        String category,
        NoticeSource source,
        String sourceUrl,
        LocalDateTime publishedAt
) {
}
