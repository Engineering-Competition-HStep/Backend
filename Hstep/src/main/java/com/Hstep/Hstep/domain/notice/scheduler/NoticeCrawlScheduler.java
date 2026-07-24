package com.Hstep.Hstep.domain.notice.scheduler;

import com.Hstep.Hstep.domain.notice.service.NoticeCrawlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.notice-crawler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NoticeCrawlScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NoticeCrawlScheduler.class);

    private final NoticeCrawlService noticeCrawlService;
    private final int scheduledPages;

    public NoticeCrawlScheduler(
            NoticeCrawlService noticeCrawlService,
            @Value("${app.notice-crawler.scheduled-pages:3}")
            int scheduledPages
    ) {
        this.noticeCrawlService = noticeCrawlService;
        this.scheduledPages = Math.max(1, scheduledPages);
    }

    @Scheduled(
            cron = "${app.notice-crawler.cron:0 */30 * * * *}",
            zone = "Asia/Seoul"
    )
    public void crawlNotices() {
        NoticeCrawlService.CrawlSummary summary =
                noticeCrawlService.crawlAll(scheduledPages);

        log.info(
                "한성대 공지 크롤링 완료: skipped={}, collected={}, inserted={}, updated={}, unchanged={}, errors={}",
                summary.skipped(),
                summary.collected(),
                summary.inserted(),
                summary.updated(),
                summary.unchanged(),
                summary.errors().size()
        );
    }
}
