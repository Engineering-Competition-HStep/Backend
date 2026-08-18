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
     *     <li>자동 시드는 templateVersion이 낮을 때만 최신 정의로 갱신합니다.</li>
     *     <li>누락된 자동 시드만 추가합니다.</li>
     *     <li>관리자 API로 직접 등록한 seedKey가 없는 항목은 수정하거나 삭제하지 않습니다.</li>
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
        int manualItemCount = 0;

        for (AiRoadmapStandardItem item : currentItems) {
            item.backfillLegacyMetadata();
            Long jobId = item.getJob().getJobId();

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

        int expectedTemplateCount = jobs.stream()
                .mapToInt(job -> AiRoadmapStandardSeedCatalog.createFor(job).size())
                .sum();
        int alreadySeededCount = 0;
        int updatedSeededCount = 0;
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

                AiRoadmapStandardItem seededItem = itemBySeedIdentity.get(seedIdentity);
                if (seededItem != null) {
                    if (seededItem.getTemplateVersion() == null
                            || seededItem.getTemplateVersion() < seed.templateVersion()) {
                        seededItem.synchronizeTemplate(seed.category(), seed.targetGrade(), seed.priority(),
                                seed.displayOrder(), seed.title(), seed.description(), seed.keyword(),
                                seed.recommendationReason(), seed.externalUrl(), seed.requiredItem(),
                                seed.roadmapLane(), seed.itemType(), seed.targetStage(), seed.coreItem(),
                                seed.defaultIncluded(), seed.templateVersion());
                        updatedSeededCount++;
                    }
                    alreadySeededCount++;
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
                                seed.requiredItem(),
                                seed.roadmapLane(),
                                seed.itemType(),
                                seed.targetStage(),
                                seed.coreItem(),
                                seed.defaultIncluded(),
                                seed.templateVersion()
                        );

                missingItems.add(missingItem);
                itemBySeedIdentity.put(seedIdentity, missingItem);
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

        if (updatedSeededCount > 0 && missingItems.isEmpty()) {
            standardItemRepository.flush();
        }

        log.info(
                "AI 표준 로드맵 동기화 완료. jobs={}, expectedTemplates={}, "
                        + "alreadySeeded={}, updatedSeeded={}, created={}, preservedManualItems={}, totalRowsAfterSync={}",
                jobs.size(),
                expectedTemplateCount,
                alreadySeededCount,
                updatedSeededCount,
                createdCount,
                Math.max(0, manualItemCount),
                currentItems.size() + createdCount
        );
    }

    private record SeedIdentity(Long jobId, String seedKey) {
    }

}
