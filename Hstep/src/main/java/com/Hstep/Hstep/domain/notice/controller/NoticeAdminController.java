package com.Hstep.Hstep.domain.notice.controller;

import com.Hstep.Hstep.domain.notice.service.NoticeCrawlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notices")
public class NoticeAdminController {

    private final NoticeCrawlService noticeCrawlService;

    public NoticeAdminController(
            NoticeCrawlService noticeCrawlService
    ) {
        this.noticeCrawlService = noticeCrawlService;
    }

    @PostMapping("/crawl")
    public NoticeCrawlService.CrawlSummary crawl(
            @RequestParam(defaultValue = "1")
            int pages
    ) {
        return noticeCrawlService.crawlAll(pages);
    }
}
