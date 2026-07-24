package com.Hstep.Hstep.domain.notice.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record NoticePageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> NoticePageResponse<T> from(Page<T> result) {
        return new NoticePageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }
}
