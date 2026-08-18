package com.Hstep.Hstep.domain.airoadmap.initializer;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.initializer.AiRoadmapStandardSeedCatalog.StandardItemSeed;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapStandardItemRepository;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(
        name = "app.ai-roadmap-standard-seed.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AiRoadmapStandardItemDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(AiRoadmapStandardItemDataInitializer.class);

    private static final int SAVE_BATCH_SIZE = 500;

    private final JobRepository jobRepository;
    private final AiRoadmapStandardItemRepository standardItemRepository;

    public AiRoadmapStandardItemDataInitializer(
            JobRepository jobRepository,
            AiRoadmapStandardItemRepository standardItemRepository
    ) {
        this.jobRepository = jobRepository;
        this.standardItemRepository = standardItemRepository;
    }

    /**
     * TRACK → JOB/JOB_TRACK 초기화가 끝난 뒤 모든 JOB의 표준 AI 로드맵을 보완합니다.
     *
     * <p>동작 원칙:</p>
     * <ul>
     *     <li>직무별 seedKey가 이미 존재하면 그대로 유지합니다.</li>
     *     <li>과거에 동일 제목·학년·카테고리로 수동 삽입한 행은 중복 생성하지 않고 seedKey만 부여합니다.</li>
     *     <li>누락된 항목만 추가하며, 기존 관리자가 수정한 표준 항목의 내용은 덮어쓰지 않습니다.</li>
     *     <li>관리자 API로 직접 등록한 seedKey가 없는 항목도 삭제하지 않습니다.</li>
     * </ul>
     */
    @Order(300)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        List<Job> jobs = jobRepository.findAllByOrderByJobNameAsc();

        if (jobs.isEmpty()) {
            log.warn("JOB 데이터가 없어 AI 표준 로드맵 초기화를 수행하지 않았습니다.");
            return;
        }

        List<AiRoadmapStandardItem> currentItems =
                standardItemRepository.findAllWithJob();

        Map<SeedIdentity, AiRoadmapStandardItem> itemBySeedIdentity =
                new HashMap<>();
        Map<NaturalIdentity, AiRoadmapStandardItem> itemByNaturalIdentity =
                new LinkedHashMap<>();

        int manualItemCount = 0;

        for (AiRoadmapStandardItem item : currentItems) {
            Long jobId = item.getJob().getJobId();

            itemByNaturalIdentity.putIfAbsent(
                    NaturalIdentity.of(jobId, item),
                    item
            );

            if (item.getSeedKey() == null || item.getSeedKey().isBlank()) {
                manualItemCount++;
                continue;
            }

            SeedIdentity identity =
                    new SeedIdentity(jobId, item.getSeedKey());

            AiRoadmapStandardItem duplicated =
                    itemBySeedIdentity.putIfAbsent(identity, item);

            if (duplicated != null) {
                log.error(
                        "중복된 AI 표준 로드맵 seedKey를 발견했습니다. jobId={}, seedKey={}, firstItemId={}, duplicateItemId={}",
                        jobId,
                        item.getSeedKey(),
                        duplicated.getStandardItemId(),
                        item.getStandardItemId()
                );
            }
        }

        int expectedTemplateCount =
                jobs.size() * AiRoadmapStandardSeedCatalog.ITEMS_PER_JOB;
        int alreadySeededCount = 0;
        int claimedLegacyCount = 0;
        int createdCount = 0;

        List<AiRoadmapStandardItem> missingItems =
                new java.util.ArrayList<>(Math.max(
                        0,
                        expectedTemplateCount - itemBySeedIdentity.size()
                ));

        for (Job job : jobs) {
            List<StandardItemSeed> seeds =
                    AiRoadmapStandardSeedCatalog.createFor(job);

            for (StandardItemSeed seed : seeds) {
                SeedIdentity seedIdentity =
                        new SeedIdentity(job.getJobId(), seed.seedKey());

                if (itemBySeedIdentity.containsKey(seedIdentity)) {
                    alreadySeededCount++;
                    continue;
                }

                NaturalIdentity naturalIdentity =
                        NaturalIdentity.of(job.getJobId(), seed);

                AiRoadmapStandardItem legacyItem =
                        itemByNaturalIdentity.get(naturalIdentity);

                if (legacyItem != null
                        && (legacyItem.getSeedKey() == null
                        || legacyItem.getSeedKey().isBlank())) {
                    legacyItem.assignSeedKeyIfAbsent(seed.seedKey());
                    itemBySeedIdentity.put(seedIdentity, legacyItem);
                    claimedLegacyCount++;
                    manualItemCount--;
                    continue;
                }

                AiRoadmapStandardItem missingItem =
                        AiRoadmapStandardItem.createSeeded(
                                job,
                                seed.seedKey(),
                                seed.category(),
                                seed.targetGrade(),
                                seed.priority(),
                                seed.displayOrder(),
                                seed.title(),
                                seed.description(),
                                seed.keyword(),
                                seed.recommendationReason(),
                                seed.externalUrl(),
                                seed.requiredItem()
                        );

                missingItems.add(missingItem);
                itemBySeedIdentity.put(seedIdentity, missingItem);
                itemByNaturalIdentity.put(naturalIdentity, missingItem);
            }
        }

        for (int start = 0; start < missingItems.size(); start += SAVE_BATCH_SIZE) {
            int end = Math.min(start + SAVE_BATCH_SIZE, missingItems.size());
            List<AiRoadmapStandardItem> batch =
                    missingItems.subList(start, end);

            standardItemRepository.saveAll(batch);
            standardItemRepository.flush();
            createdCount += batch.size();
        }

        // 기존 수동 행에 seedKey만 부여한 경우에도 변경 사항을 즉시 DB에 반영합니다.
        if (claimedLegacyCount > 0 && missingItems.isEmpty()) {
            standardItemRepository.flush();
        }

        log.info(
                "AI 표준 로드맵 동기화 완료. jobs={}, templatesPerJob={}, expectedTemplates={}, "
                        + "alreadySeeded={}, claimedLegacy={}, created={}, preservedManualItems={}, totalRowsAfterSync={}",
                jobs.size(),
                AiRoadmapStandardSeedCatalog.ITEMS_PER_JOB,
                expectedTemplateCount,
                alreadySeededCount,
                claimedLegacyCount,
                createdCount,
                Math.max(0, manualItemCount),
                currentItems.size() + createdCount
        );
    }

    private record SeedIdentity(Long jobId, String seedKey) {
    }

    private record NaturalIdentity(
            Long jobId,
            AiRoadmapStandardItem.Category category,
            Integer targetGrade,
            String title
    ) {
        private static NaturalIdentity of(
                Long jobId,
                AiRoadmapStandardItem item
        ) {
            return new NaturalIdentity(
                    jobId,
                    item.getCategory(),
                    item.getTargetGrade(),
                    item.getTitle()
            );
        }

        private static NaturalIdentity of(
                Long jobId,
                StandardItemSeed seed
        ) {
            return new NaturalIdentity(
                    jobId,
                    seed.category(),
                    seed.targetGrade(),
                    seed.title()
            );
        }
    }
}
