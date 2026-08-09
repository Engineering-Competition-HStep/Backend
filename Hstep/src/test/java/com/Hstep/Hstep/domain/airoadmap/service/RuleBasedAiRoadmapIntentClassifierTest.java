package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedAiRoadmapIntentClassifierTest {

    private RuleBasedAiRoadmapIntentClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new RuleBasedAiRoadmapIntentClassifier();
    }

    @Test
    void 지원하는_변경_요청을_허용된_동작으로_분류한다() {
        assertThat(classifier.classify("관심 직무를 변경하고 싶어"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.CHANGE_INTEREST_JOB);
        assertThat(classifier.classify("프로젝트를 추가해줘"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.ADD_ROADMAP_ITEM);
        assertThat(classifier.classify("이 활동 완료했어"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.COMPLETE_ROADMAP_ITEM);
        assertThat(classifier.classify("이 활동은 로드맵에서 제외해줘"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.HIDE_ROADMAP_ITEM);
        assertThat(classifier.classify("이 항목 우선순위를 높여줘"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.CHANGE_PRIORITY);
        assertThat(classifier.classify("이 활동을 왜 해야 하는지 설명해줘"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.EXPLAIN_ROADMAP_ITEM);
    }

    @Test
    void 우선_활동_질문과_지원하지_않는_질문은_데이터_변경_동작으로_분류하지_않는다() {
        assertThat(classifier.classify("현재 가장 먼저 해야 할 활동은 뭐야?"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.NO_ACTION);
        assertThat(classifier.classify("나랑 가장 잘 맞는 회사 알려줘"))
                .isEqualTo(AiRoadmapChangeProposal.ActionType.NO_ACTION);
    }
}
