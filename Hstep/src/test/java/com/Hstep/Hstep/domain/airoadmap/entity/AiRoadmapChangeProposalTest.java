package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import com.Hstep.Hstep.domain.member.entity.Member;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AiRoadmapChangeProposalTest {

    @Test
    void 변경_전후_payload를_JSON으로_보관하고_복원한다() {
        Member member = Member.create("user", "user@test.com", "encoded", "사용자", 3);
        AiRoadmap roadmap = AiRoadmap.create(member,
                Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트"));

        AiRoadmapChangeProposal proposal = AiRoadmapChangeProposal.create(member, roadmap,
                AiRoadmapChangeProposal.ActionType.MOVE_ITEM, 1L, null, null, null,
                "이동", Map.of("targetStage", "GRADE_3"),
                Map.of("targetStage", "GRADE_4", "displayOrder", 20));

        assertThat(proposal.readBeforeSnapshot()).containsEntry("targetStage", "GRADE_3");
        assertThat(proposal.readAfterSnapshot())
                .containsEntry("targetStage", "GRADE_4")
                .containsEntry("displayOrder", 20);
        assertThat(proposal.getStatus()).isEqualTo(AiRoadmapChangeProposal.Status.PENDING);
    }
}
