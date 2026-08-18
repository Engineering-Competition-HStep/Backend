package com.Hstep.Hstep.domain.airoadmap.initializer;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.initializer.AiRoadmapStandardSeedCatalog.StandardItemSeed;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapStandardItemRepository;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import com.Hstep.Hstep.domain.job.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiRoadmapStandardItemDataInitializerTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private AiRoadmapStandardItemRepository standardItemRepository;

    private AiRoadmapStandardItemDataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new AiRoadmapStandardItemDataInitializer(
                jobRepository,
                standardItemRepository
        );
    }

    @Test
    void 표준_로드맵이_없는_직무에_전체_항목을_생성한다() {
        Job job = createJob(1L, "백엔드 개발자", JobCategory.SOFTWARE);

        when(jobRepository.findAllByOrderByJobNameAsc())
                .thenReturn(List.of(job));
        when(standardItemRepository.findAllWithJob())
                .thenReturn(List.of());

        initializer.initialize();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AiRoadmapStandardItem>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(standardItemRepository).saveAll(captor.capture());
        verify(standardItemRepository).flush();

        List<AiRoadmapStandardItem> savedItems = captor.getValue();

        assertThat(savedItems)
                .hasSize(AiRoadmapStandardSeedCatalog.ITEMS_PER_JOB)
                .allSatisfy(item -> assertThat(item.getJob()).isSameAs(job));

        assertThat(savedItems)
                .extracting(AiRoadmapStandardItem::getSeedKey)
                .doesNotHaveDuplicates()
                .doesNotContainNull();
    }

    @Test
    void 모든_seedKey가_이미_있으면_추가_저장을_하지_않는다() {
        Job job = createJob(1L, "데이터 분석가", JobCategory.DATA_AI);

        List<AiRoadmapStandardItem> existingItems =
                AiRoadmapStandardSeedCatalog.createFor(job).stream()
                        .map(seed -> toSeededEntity(job, seed))
                        .toList();

        when(jobRepository.findAllByOrderByJobNameAsc())
                .thenReturn(List.of(job));
        when(standardItemRepository.findAllWithJob())
                .thenReturn(existingItems);

        initializer.initialize();

        verify(standardItemRepository, never()).saveAll(anyList());
        verify(standardItemRepository, never()).flush();
    }

    @Test
    void 동일한_자연키의_기존_수동_항목은_중복_생성하지_않고_seedKey를_부여한다() {
        Job job = createJob(1L, "백엔드 개발자", JobCategory.SOFTWARE);
        StandardItemSeed legacySeed =
                AiRoadmapStandardSeedCatalog.createFor(job).getFirst();

        AiRoadmapStandardItem legacyItem =
                AiRoadmapStandardItem.create(
                        job,
                        legacySeed.category(),
                        legacySeed.targetGrade(),
                        legacySeed.priority(),
                        legacySeed.displayOrder(),
                        legacySeed.title(),
                        legacySeed.description(),
                        legacySeed.keyword(),
                        legacySeed.recommendationReason(),
                        legacySeed.externalUrl(),
                        legacySeed.requiredItem()
                );

        when(jobRepository.findAllByOrderByJobNameAsc())
                .thenReturn(List.of(job));
        when(standardItemRepository.findAllWithJob())
                .thenReturn(List.of(legacyItem));

        initializer.initialize();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AiRoadmapStandardItem>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(standardItemRepository).saveAll(captor.capture());
        verify(standardItemRepository).flush();

        assertThat(legacyItem.getSeedKey())
                .isEqualTo(legacySeed.seedKey());
        assertThat(captor.getValue())
                .hasSize(AiRoadmapStandardSeedCatalog.ITEMS_PER_JOB - 1);
    }

    @Test
    void 직무가_없으면_초기화를_건너뛴다() {
        when(jobRepository.findAllByOrderByJobNameAsc())
                .thenReturn(List.of());

        initializer.initialize();

        verify(standardItemRepository, never()).findAllWithJob();
        verify(standardItemRepository, never()).saveAll(anyList());
        verify(standardItemRepository, never()).flush();
    }

    private Job createJob(Long jobId, String jobName, JobCategory category) {
        Job job = Job.create(jobName, category, "테스트 직무");
        ReflectionTestUtils.setField(job, "jobId", jobId);
        return job;
    }

    private AiRoadmapStandardItem toSeededEntity(
            Job job,
            StandardItemSeed seed
    ) {
        return AiRoadmapStandardItem.createSeeded(
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
    }
}
