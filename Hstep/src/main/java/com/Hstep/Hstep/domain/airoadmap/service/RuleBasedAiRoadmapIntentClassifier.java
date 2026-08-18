package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RuleBasedAiRoadmapIntentClassifier implements AiRoadmapIntentClassifier {

    @Override
    public AiRoadmapChangeProposal.ActionType classify(String message) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT).replace(" ", "");

        if (containsAny(normalized, "관심직무변경", "관심직무를변경", "직무변경", "직무를변경", "진로변경", "진로를변경", "다른직무")) {
            return AiRoadmapChangeProposal.ActionType.CHANGE_INTEREST_JOB;
        }
        if (containsAny(normalized, "완료취소", "다시진행", "미완료로", "완료를취소")) {
            return AiRoadmapChangeProposal.ActionType.REOPEN_ITEM;
        }
        if (containsAny(normalized, "완료", "끝냈", "취득했", "했어", "마쳤")) {
            return AiRoadmapChangeProposal.ActionType.COMPLETE_ITEM;
        }
        if (containsAny(normalized, "제외", "숨겨", "빼줘", "삭제")) {
            return AiRoadmapChangeProposal.ActionType.REMOVE_ITEM;
        }
        if (containsAny(normalized, "우선순위변경", "우선순위를", "우선순위높", "우선순위낮")) {
            return AiRoadmapChangeProposal.ActionType.CHANGE_PRIORITY;
        }
        if (containsAny(normalized, "왜해야", "설명해", "준비방법", "뭐하는", "어떤활동")) {
            return AiRoadmapChangeProposal.ActionType.EXPLAIN_ROADMAP_ITEM;
        }
        if (containsAny(normalized, "옮겨", "이동", "학년으로", "영역으로")) {
            return AiRoadmapChangeProposal.ActionType.MOVE_ITEM;
        }
        if (containsAny(normalized, "교체", "대신", "바꿔줘")) {
            return AiRoadmapChangeProposal.ActionType.REPLACE_ITEM;
        }
        if (containsAny(normalized, "제목수정", "설명수정", "내용수정")) {
            return AiRoadmapChangeProposal.ActionType.EDIT_ITEM;
        }
        if (containsAny(normalized, "프로젝트", "자격증", "추가해", "추가해줘", "추천해", "추천해줘")) {
            return AiRoadmapChangeProposal.ActionType.ADD_CUSTOM_ITEM;
        }
        if (containsAny(normalized, "가장먼저", "먼저해야", "우선활동", "뭐부터", "지금해야")) {
            return AiRoadmapChangeProposal.ActionType.NO_ACTION;
        }
        return AiRoadmapChangeProposal.ActionType.NO_ACTION;
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
