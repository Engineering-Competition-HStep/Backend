package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import com.Hstep.Hstep.domain.member.entity.Member;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiRoadmapItemTest {

    @Test
    void 표준_항목을_개인_스냅샷으로_복사해_이후_표준_수정과_격리한다() {
        Member member = Member.create("user", "user@test.com", "encoded", "사용자", 3);
        Job job = Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트");
        AiRoadmap roadmap = AiRoadmap.create(member, job);
        AiRoadmapStandardItem standard = standard(job, "Spring Boot REST API");

        AiRoadmapItem item = AiRoadmapItem.fromStandard(
                roadmap, standard, AiRoadmapItem.Status.PENDING, false);
        standard.update(AiRoadmapStandardItem.Category.COURSE, 4,
                AiRoadmapStandardItem.Priority.LOW, 99, "변경된 공용 제목", "변경된 설명",
                "변경", null, null, true, RoadmapLane.LEARNING, RoadmapItemType.FRAMEWORK,
                RoadmapStage.GRADE_4, true, true);

        assertThat(item.getTitle()).isEqualTo("Spring Boot REST API");
        assertThat(item.getTargetStage()).isEqualTo(RoadmapStage.GRADE_3);
        assertThat(item.getRoadmapLane()).isEqualTo(RoadmapLane.PROJECT);
        assertThat(item.getSourceType()).isEqualTo(RoadmapItemSourceType.STANDARD_TEMPLATE);
    }

    @Test
    void AI_추가_항목은_표준_항목_없이_개인_값을_편집할_수_있다() {
        Member member = Member.create("user", "user@test.com", "encoded", "사용자", 3);
        Job job = Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트");
        AiRoadmapItem item = AiRoadmapItem.createCustom(AiRoadmap.create(member, job),
                "메시지 큐 실습", "비동기 흐름 구현", RoadmapLane.PROJECT,
                RoadmapItemType.MINI_PROJECT, RoadmapStage.GRADE_3, 10,
                AiRoadmapStandardItem.Priority.MEDIUM);

        item.edit("이벤트 기반 주문 처리", "재시도와 멱등성 포함");
        item.move(RoadmapStage.GRADE_4, RoadmapLane.PROJECT, 20);

        assertThat(item.getStandardItem()).isNull();
        assertThat(item.getSourceType()).isEqualTo(RoadmapItemSourceType.AI_ADDED);
        assertThat(item.getTitle()).isEqualTo("이벤트 기반 주문 처리");
        assertThat(item.getTargetStage()).isEqualTo(RoadmapStage.GRADE_4);
        assertThat(item.getDisplayOrder()).isEqualTo(20);
    }

    private AiRoadmapStandardItem standard(Job job, String title) {
        return AiRoadmapStandardItem.createSeeded(job, "TEST", AiRoadmapStandardItem.Category.PROJECT,
                3, AiRoadmapStandardItem.Priority.HIGH, 10, title, "설명", "Spring, REST",
                "추천", null, true, RoadmapLane.PROJECT, RoadmapItemType.MINI_PROJECT,
                RoadmapStage.GRADE_3, true, true, 2);
    }
}
