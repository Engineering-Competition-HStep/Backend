package com.Hstep.Hstep.domain.notice.controller;

import com.Hstep.Hstep.domain.notice.dto.NoticePageResponse;
import com.Hstep.Hstep.domain.notice.dto.NoticeResponse;
import com.Hstep.Hstep.domain.notice.dto.NoticeSearchCondition;
import com.Hstep.Hstep.domain.notice.entity.NoticeSource;
import com.Hstep.Hstep.domain.notice.service.NoticeQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeQueryService noticeQueryService;

    public NoticeController(
            NoticeQueryService noticeQueryService
    ) {
        this.noticeQueryService = noticeQueryService;
    }

    @GetMapping
    public NoticePageResponse<NoticeResponse> search(
            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            NoticeSource source,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(defaultValue = "publishedAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String direction
    ) {
        NoticeSearchCondition condition =
                new NoticeSearchCondition(
                        keyword,
                        category,
                        source,
                        startDate,
                        endDate
                );

        return noticeQueryService.search(
                condition,
                page,
                size,
                sortBy,
                direction
        );
    }

    @GetMapping("/{noticeId}")
    public NoticeResponse getById(
            @PathVariable Long noticeId
    ) {
        return noticeQueryService.getById(noticeId);
    }

    @GetMapping("/latest")
    public List<NoticeResponse> getLatest(
            @RequestParam(required = false)
            NoticeSource source,

            @RequestParam(defaultValue = "5")
            int size
    ) {
        return noticeQueryService.getLatest(source, size);
    }

    @GetMapping("/categories")
    public List<String> getCategories(
            @RequestParam(required = false)
            NoticeSource source
    ) {
        return noticeQueryService.getCategories(source);
    }
}
