package com.Hstep.Hstep.domain.notice.dto;

import com.Hstep.Hstep.domain.notice.entity.HansungNotice;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long noticeId,
        String title,
        String category,
        String source,
        String sourceName,
        String sourceUrl,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static NoticeResponse from(HansungNotice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getCategory(),
                notice.getSource().name(),
                notice.getSource().getDisplayName(),
                notice.getSourceUrl(),
                notice.getPublishedAt(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
