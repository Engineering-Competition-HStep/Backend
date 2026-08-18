package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.*;
import com.Hstep.Hstep.domain.airoadmap.repository.*;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import com.Hstep.Hstep.domain.job.repository.JobRepository;
import com.Hstep.Hstep.domain.job.repository.JobTrackRepository;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiRoadmapServiceSummaryTest {

    private final AiRoadmapItemRepository itemRepository = mock(AiRoadmapItemRepository.class);
    private AiRoadmapService service;
    private Member member;
    private AiRoadmap roadmap;

    @BeforeEach
    void setUp() {
        service = new AiRoadmapService(mock(AiRoadmapRepository.class), itemRepository,
                mock(AiRoadmapStandardItemRepository.class), mock(JobRepository.class),
                mock(JobTrackRepository.class), mock(AiRoadmapProfileAnalyzer.class),
                mock(RoadmapEvidenceMatcher.class), mock(JobRecommendationProfileCatalog.class),
                mock(TrackRepository.class));
        member = Member.create("user", "user@test.com", "encoded", "사용자", 3);
        member.updateProfile(null, null, new BigDecimal("3.50"));
        roadmap = AiRoadmap.create(member, Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트"));
    }

    @Test
    void 숨김을_제외하고_진행률과_영역별_개수를_계산한다() {
        AiRoadmapItem completed = item("Java", RoadmapLane.LEARNING, RoadmapStage.GRADE_2, 10);
        completed.complete(false);
        AiRoadmapItem needs = item("API 프로젝트", RoadmapLane.PROJECT, RoadmapStage.GRADE_3, 20);
        org.springframework.test.util.ReflectionTestUtils.setField(needs, "status",
                AiRoadmapItem.Status.NEEDS_IMPROVEMENT);
        AiRoadmapItem pending = item("SQLD", RoadmapLane.CERTIFICATION, RoadmapStage.GRADE_3, 30);
        AiRoadmapItem hidden = item("숨김", RoadmapLane.EXPERIENCE, RoadmapStage.GRADE_4, 40);
        hidden.hide();
        when(itemRepository.findAllByAiRoadmap_AiRoadmapId(null))
                .thenReturn(List.of(completed, needs, pending, hidden));

        AiRoadmapDto.RoadmapResponse response = service.toRoadmapResponse(member, roadmap, null, null);

        assertThat(response.items()).hasSize(3);
        assertThat(response.summary().totalCount()).isEqualTo(3);
        assertThat(response.summary().completedCount()).isEqualTo(1);
        assertThat(response.summary().needsImprovementCount()).isEqualTo(1);
        assertThat(response.summary().pendingCount()).isEqualTo(1);
        assertThat(response.summary().progressRate()).isEqualTo(33);
        assertThat(response.summary().laneCounts())
                .containsEntry(RoadmapLane.LEARNING, 1L)
                .containsEntry(RoadmapLane.EXPERIENCE, 0L);
    }

    @Test
    void 신규_stage와_lane_필터를_함께_적용한다() {
        AiRoadmapItem target = item("팀 프로젝트", RoadmapLane.PROJECT, RoadmapStage.GRADE_3, 10);
        AiRoadmapItem otherStage = item("포트폴리오", RoadmapLane.PROJECT, RoadmapStage.GRADE_4, 20);
        AiRoadmapItem otherLane = item("JPA", RoadmapLane.LEARNING, RoadmapStage.GRADE_3, 30);
        when(itemRepository.findAllByAiRoadmap_AiRoadmapId(null))
                .thenReturn(List.of(target, otherStage, otherLane));

        AiRoadmapDto.RoadmapResponse response = service.toRoadmapResponse(
                member, roadmap, null, null, RoadmapStage.GRADE_3, RoadmapLane.PROJECT);

        assertThat(response.items()).extracting(AiRoadmapDto.ItemResponse::title)
                .containsExactly("팀 프로젝트");
    }

    private AiRoadmapItem item(String title, RoadmapLane lane, RoadmapStage stage, int order) {
        return AiRoadmapItem.createCustom(roadmap, title, "설명", lane, RoadmapItemType.OTHER,
                stage, order, AiRoadmapStandardItem.Priority.MEDIUM);
    }
}
