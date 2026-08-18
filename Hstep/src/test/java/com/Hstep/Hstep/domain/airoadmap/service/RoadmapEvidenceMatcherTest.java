package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.entity.*;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoadmapEvidenceMatcherTest {

    private final AiRoadmapProfileAnalyzer analyzer = mock(AiRoadmapProfileAnalyzer.class);
    private final Job job = Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트");
    private RoadmapEvidenceMatcher matcher;

    @BeforeEach
    void setUp() {
        matcher = new RoadmapEvidenceMatcher(analyzer);
        when(analyzer.certificateNames("user")).thenReturn(Set.of());
        when(analyzer.activityEvidence("user")).thenReturn(List.of());
    }

    @Test
    void 등록한_자격증명이_정확히_일치할_때만_완료한다() {
        when(analyzer.certificateNames("user")).thenReturn(Set.of("sqld"));
        AiRoadmapStandardItem sqld = standard("SQLD 준비", "SQLD", RoadmapLane.CERTIFICATION,
                RoadmapItemType.CERTIFICATE);

        assertThat(matcher.resolve("user", sqld)).isEqualTo(AiRoadmapItem.Status.COMPLETED);
    }

    @Test
    void 프로젝트의_SQL_단어만으로_SQLD를_완료하지_않는다() {
        when(analyzer.activityEvidence("user")).thenReturn(List.of("팀 프로젝트에서 SQL 사용"));
        AiRoadmapStandardItem sqld = standard("SQLD 준비", "SQLD", RoadmapLane.CERTIFICATION,
                RoadmapItemType.CERTIFICATE);

        assertThat(matcher.resolve("user", sqld)).isEqualTo(AiRoadmapItem.Status.PENDING);
    }

    @Test
    void 학습_기술의_일부_사용_증거는_개선_필요로_판정한다() {
        when(analyzer.activityEvidence("user")).thenReturn(List.of("Java Spring 팀 프로젝트"));
        AiRoadmapStandardItem learning = standard("Spring Boot 계층형 아키텍처", "Spring, 계층형",
                RoadmapLane.LEARNING, RoadmapItemType.FRAMEWORK);

        assertThat(matcher.resolve("user", learning)).isEqualTo(AiRoadmapItem.Status.NEEDS_IMPROVEMENT);
    }

    @Test
    void 취업_준비_항목은_증거와_무관하게_기본_미완료다() {
        when(analyzer.activityEvidence("user")).thenReturn(List.of("이력서 면접 준비 완료"));
        AiRoadmapStandardItem item = standard("이력서·자기소개서·모의 면접", "이력서, 면접",
                RoadmapLane.EXPERIENCE, RoadmapItemType.JOB_PREPARATION);

        assertThat(matcher.resolve("user", item)).isEqualTo(AiRoadmapItem.Status.PENDING);
    }

    private AiRoadmapStandardItem standard(String title, String keyword, RoadmapLane lane,
                                           RoadmapItemType type) {
        AiRoadmapStandardItem.Category category = lane == RoadmapLane.CERTIFICATION
                ? AiRoadmapStandardItem.Category.CERTIFICATE : AiRoadmapStandardItem.Category.COURSE;
        return AiRoadmapStandardItem.createSeeded(job, title, category, 3,
                AiRoadmapStandardItem.Priority.HIGH, 10, title, "설명", keyword, "추천", null,
                true, lane, type, RoadmapStage.GRADE_3, true, true, 2);
    }
}
