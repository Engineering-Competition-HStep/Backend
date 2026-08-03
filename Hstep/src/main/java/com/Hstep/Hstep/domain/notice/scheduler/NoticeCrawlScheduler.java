package com.Hstep.Hstep.domain.notice.scheduler;

import com.Hstep.Hstep.domain.notice.service.NoticeCrawlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
    private final boolean runOnStartup;

    public NoticeCrawlScheduler(
            NoticeCrawlService noticeCrawlService,

            @Value("${app.notice-crawler.scheduled-pages:3}")
            int scheduledPages,

            @Value("${app.notice-crawler.run-on-startup:true}")
            boolean runOnStartup
    ) {
        this.noticeCrawlService = noticeCrawlService;
        this.scheduledPages = Math.max(1, scheduledPages);
        this.runOnStartup = runOnStartup;
    }

    /**
     * Spring Boot 애플리케이션이 완전히 실행된 직후 한 번 동작합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void crawlOnStartup() {
        if (!runOnStartup) {
            log.info("서버 시작 시 공지사항 크롤링이 비활성화되어 있습니다.");
            return;
        }

        executeCrawl("STARTUP");
    }

    /**
     * 설정된 주기에 따라 반복 실행됩니다.
     */
    @Scheduled(
            cron = "${app.notice-crawler.cron:0 */30 * * * *}",
            zone = "Asia/Seoul"
    )
    public void crawlBySchedule() {
        executeCrawl("SCHEDULED");
    }

    private void executeCrawl(String trigger) {
        log.info(
                "한성대학교 공지사항 크롤링 시작: trigger={}, pages={}",
                trigger,
                scheduledPages
        );

        try {
            NoticeCrawlService.CrawlSummary summary =
                    noticeCrawlService.crawlAll(scheduledPages);

            log.info(
                    """
                    한성대학교 공지사항 크롤링 완료
                    trigger={}
                    skipped={}
                    collected={}
                    inserted={}
                    updated={}
                    unchanged={}
                    errors={}
                    """,
                    trigger,
                    summary.skipped(),
                    summary.collected(),
                    summary.inserted(),
                    summary.updated(),
                    summary.unchanged(),
                    summary.errors()
            );
        } catch (Exception exception) {
            log.error(
                    "한성대학교 공지사항 크롤링 중 오류 발생: trigger={}",
                    trigger,
                    exception
            );
        }
    }
}