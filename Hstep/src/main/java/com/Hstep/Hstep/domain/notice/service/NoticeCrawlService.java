package com.Hstep.Hstep.domain.notice.service;

import com.Hstep.Hstep.domain.notice.crawler.CrawledNotice;
import com.Hstep.Hstep.domain.notice.crawler.HansungNoticeCrawler;
import com.Hstep.Hstep.domain.notice.crawler.NoticeBoard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class NoticeCrawlService {

    private static final Logger log =
            LoggerFactory.getLogger(NoticeCrawlService.class);

    private final HansungNoticeCrawler crawler;
    private final NoticeUpsertService upsertService;
    private final long delayMs;
    private final int maxManualPages;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public NoticeCrawlService(
            HansungNoticeCrawler crawler,
            NoticeUpsertService upsertService,
            @Value("${app.notice-crawler.delay-ms:500}")
            long delayMs,
            @Value("${app.notice-crawler.max-manual-pages:50}")
            int maxManualPages
    ) {
        this.crawler = crawler;
        this.upsertService = upsertService;
        this.delayMs = Math.max(0, delayMs);
        this.maxManualPages = Math.max(1, maxManualPages);
    }

    public CrawlSummary crawlAll(int requestedPages) {
        int pages = Math.max(
                1,
                Math.min(requestedPages, maxManualPages)
        );

        if (!running.compareAndSet(false, true)) {
            return CrawlSummary.skipped(
                    "이미 공지사항 크롤링이 실행 중입니다."
            );
        }

        int collected = 0;
        int inserted = 0;
        int updated = 0;
        int unchanged = 0;
        List<String> errors = new ArrayList<>();

        try {
            for (NoticeBoard board : NoticeBoard.values()) {
                for (int page = 1; page <= pages; page++) {
                    try {
                        List<CrawledNotice> crawled =
                                crawler.crawl(board, page);

                        if (crawled.isEmpty()) {
                            break;
                        }

                        NoticeUpsertService.UpsertResult result =
                                upsertService.upsert(crawled);

                        collected += crawled.size();
                        inserted += result.inserted();
                        updated += result.updated();
                        unchanged += result.unchanged();

                        sleepBetweenRequests();
                    } catch (Exception exception) {
                        String message =
                                board.name()
                                        + " "
                                        + page
                                        + "페이지 수집 실패: "
                                        + exception.getMessage();

                        log.warn(message, exception);
                        errors.add(message);
                        break;
                    }
                }
            }

            return new CrawlSummary(
                    false,
                    pages,
                    collected,
                    inserted,
                    updated,
                    unchanged,
                    errors
            );
        } finally {
            running.set(false);
        }
    }

    private void sleepBetweenRequests() {
        if (delayMs <= 0) {
            return;
        }

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    public record CrawlSummary(
            boolean skipped,
            int requestedPages,
            int collected,
            int inserted,
            int updated,
            int unchanged,
            List<String> errors
    ) {
        public static CrawlSummary skipped(String reason) {
            return new CrawlSummary(
                    true,
                    0,
                    0,
                    0,
                    0,
                    0,
                    List.of(reason)
            );
        }
    }
}
