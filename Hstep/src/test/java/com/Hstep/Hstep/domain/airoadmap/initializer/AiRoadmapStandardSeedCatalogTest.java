package com.Hstep.Hstep.domain.airoadmap.initializer;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.initializer.AiRoadmapStandardSeedCatalog.StandardItemSeed;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiRoadmapStandardSeedCatalogTest {

    @Test
    void 모든_직무_카테고리에_동일한_구조의_표준_로드맵을_생성한다() {
        for (JobCategory category : JobCategory.values()) {
            List<StandardItemSeed> seeds =
                    AiRoadmapStandardSeedCatalog.createFor("테스트직무", category);

            assertThat(seeds)
                    .as("category=%s", category)
                    .hasSize(AiRoadmapStandardSeedCatalog.ITEMS_PER_JOB);

            assertThat(seeds)
                    .extracting(StandardItemSeed::seedKey)
                    .doesNotHaveDuplicates();

            assertThat(seeds)
                    .allSatisfy(seed -> {
                        assertThat(seed.seedKey()).isNotBlank();
                        assertThat(seed.title()).isNotBlank().hasSizeLessThanOrEqualTo(120);
                        assertThat(seed.description()).isNotBlank().hasSizeLessThanOrEqualTo(1000);
                        assertThat(seed.keyword()).isNotBlank().hasSizeLessThanOrEqualTo(300);
                        assertThat(seed.recommendationReason()).isNotBlank().hasSizeLessThanOrEqualTo(500);
                        assertThat(seed.targetGrade()).isBetween(2, 4);
                        assertThat(seed.displayOrder()).isPositive();
                    });

            assertThat(seeds)
                    .anyMatch(seed -> seed.targetGrade() == 2 && seed.requiredItem());
            assertThat(seeds)
                    .anyMatch(seed -> seed.targetGrade() == 3 && seed.requiredItem());
            assertThat(seeds)
                    .anyMatch(seed -> seed.targetGrade() == 4 && seed.requiredItem());

            assertThat(seeds)
                    .anyMatch(seed ->
                            seed.targetGrade() == 3
                                    && seed.category() == AiRoadmapStandardItem.Category.PROJECT
                                    && !seed.requiredItem()
                    );
            assertThat(seeds)
                    .anyMatch(seed ->
                            seed.targetGrade() == 3
                                    && seed.category() == AiRoadmapStandardItem.Category.CERTIFICATE
                                    && !seed.requiredItem()
                    );
        }
    }

    @Test
    void 기존_로컬_SQL로_등록한_대표_항목과_동일한_자연키를_유지한다() {
        List<StandardItemSeed> seeds =
                AiRoadmapStandardSeedCatalog.createFor(
                        "백엔드 개발자",
                        JobCategory.SOFTWARE
                );

        assertThat(seeds)
                .anyMatch(seed ->
                        seed.category() == AiRoadmapStandardItem.Category.COURSE
                                && seed.targetGrade() == 2
                                && seed.title().equals("백엔드 개발자 기초 역량 학습")
                );

        assertThat(seeds)
                .anyMatch(seed ->
                        seed.category() == AiRoadmapStandardItem.Category.PROJECT
                                && seed.targetGrade() == 3
                                && seed.title().equals("백엔드 개발자 포트폴리오 프로젝트")
                );

        assertThat(seeds)
                .anyMatch(seed ->
                        seed.category() == AiRoadmapStandardItem.Category.CERTIFICATE
                                && seed.targetGrade() == 3
                                && seed.title().equals("백엔드 개발자 관련 자격증 준비")
                );
    }
}
