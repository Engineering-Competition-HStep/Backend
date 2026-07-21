package com.Hstep.Hstep.domain.notice.service;

import com.Hstep.Hstep.domain.notice.crawler.CrawledNotice;
import com.Hstep.Hstep.domain.notice.entity.HansungNotice;
import com.Hstep.Hstep.domain.notice.repository.HansungNoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NoticeUpsertService {

    private final HansungNoticeRepository noticeRepository;

    public NoticeUpsertService(
            HansungNoticeRepository noticeRepository
    ) {
        this.noticeRepository = noticeRepository;
    }

    @Transactional
    public UpsertResult upsert(
            Collection<CrawledNotice> crawledNotices
    ) {
        if (crawledNotices == null || crawledNotices.isEmpty()) {
            return new UpsertResult(0, 0, 0);
        }

        Map<String, CrawledNotice> uniqueByUrl =
                crawledNotices.stream()
                        .collect(Collectors.toMap(
                                CrawledNotice::sourceUrl,
                                Function.identity(),
                                (first, second) -> second,
                                LinkedHashMap::new
                        ));

        Map<String, HansungNotice> existingByUrl =
                noticeRepository
                        .findAllBySourceUrlIn(uniqueByUrl.keySet())
                        .stream()
                        .collect(Collectors.toMap(
                                HansungNotice::getSourceUrl,
                                Function.identity()
                        ));

        List<HansungNotice> toSave = new ArrayList<>();
        int inserted = 0;
        int updated = 0;
        int unchanged = 0;

        for (CrawledNotice crawled : uniqueByUrl.values()) {
            HansungNotice existing =
                    existingByUrl.get(crawled.sourceUrl());

            if (existing == null) {
                toSave.add(HansungNotice.create(
                        crawled.title(),
                        crawled.category(),
                        crawled.source(),
                        crawled.sourceUrl(),
                        crawled.publishedAt()
                ));
                inserted++;
                continue;
            }

            boolean changed = existing.updateFrom(
                    crawled.title(),
                    crawled.category(),
                    crawled.source(),
                    crawled.publishedAt()
            );

            if (changed) {
                toSave.add(existing);
                updated++;
            } else {
                unchanged++;
            }
        }

        if (!toSave.isEmpty()) {
            noticeRepository.saveAll(toSave);
        }

        return new UpsertResult(
                inserted,
                updated,
                unchanged
        );
    }

    public record UpsertResult(
            int inserted,
            int updated,
            int unchanged
    ) {
    }
}
