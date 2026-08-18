package com.Hstep.Hstep.domain.airoadmap.initializer;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.initializer.AiRoadmapStandardSeedCatalog.StandardItemSeed;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiRoadmapStandardSeedCatalogTest {

    @Test
    void 모든_직무_카테고리에_유효한_표준_로드맵을_생성한다() {
        for (JobCategory category : JobCategory.values()) {
            List<StandardItemSeed> seeds =
                    AiRoadmapStandardSeedCatalog.createFor("테스트직무", category);

            assertThat(seeds)
                    .as("category=%s", category)
                    .hasSizeGreaterThanOrEqualTo(12);

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
                        assertThat(seed.targetGrade()).isBetween(2, 5);
                        assertThat(seed.displayOrder()).isPositive();
                        assertThat(seed.roadmapLane()).isNotNull();
                        assertThat(seed.itemType()).isNotNull();
                        assertThat(seed.targetStage()).isNotNull();
                        assertThat(seed.templateVersion()).isEqualTo(AiRoadmapStandardSeedCatalog.TEMPLATE_VERSION);
                    });

            assertThat(seeds)
                    .anyMatch(seed -> seed.targetGrade() == 2 && seed.requiredItem());
            assertThat(seeds)
                    .anyMatch(seed -> seed.targetGrade() == 3 && seed.requiredItem());
            assertThat(seeds)
                    .anyMatch(seed -> seed.targetGrade() == 4 && seed.requiredItem());

        }
    }

    @Test
    void 백엔드_직무는_구체적인_준비_활동을_생성한다() {
        List<StandardItemSeed> seeds =
                AiRoadmapStandardSeedCatalog.createFor(
                        "백엔드 개발자",
                        JobCategory.SOFTWARE
                );

        assertThat(seeds).extracting(StandardItemSeed::title)
                .anyMatch(title -> title.contains("자료구조"))
                .anyMatch(title -> title.contains("SQL"))
                .anyMatch(title -> title.contains("Git"))
                .anyMatch(title -> title.contains("Spring Boot"))
                .anyMatch(title -> title.contains("JPA"))
                .anyMatch(title -> title.contains("테스트"))
                .anyMatch(title -> title.contains("Docker"))
                .anyMatch(title -> title.contains("인턴"))
                .anyMatch(title -> title.contains("면접"));
        assertThat(seeds).extracting(StandardItemSeed::title)
                .noneMatch(title -> title.endsWith("기초 역량 학습"))
                .noneMatch(title -> title.endsWith("핵심 실무 역량 강화"))
                .noneMatch(title -> title.endsWith("포트폴리오 프로젝트"));
        assertThat(seeds).filteredOn(seed -> seed.title().equals("SQLD 준비"))
                .singleElement().satisfies(seed -> {
                    assertThat(seed.coreItem()).isFalse();
                    assertThat(seed.defaultIncluded()).isTrue();
                });
    }
}
