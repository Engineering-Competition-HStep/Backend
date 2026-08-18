package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeminiAiRoadmapIntentClassifierTest {

    private final ChatModel chatModel = mock(ChatModel.class);
    private final GeminiAiRoadmapIntentClassifier classifier =
            new GeminiAiRoadmapIntentClassifier(chatModel, JsonMapper.builder().build());

    @Test
    void Gemini응답을_관심직무변경_요청으로_분류한다() {
        when(chatModel.call(anyString()))
                .thenReturn("{\"actionType\":\"CHANGE_INTEREST_JOB\"}");

        AiRoadmapChangeProposal.ActionType result = classifier.classify("관심 직무를 바꾸고 싶어");

        assertEquals(AiRoadmapChangeProposal.ActionType.CHANGE_INTEREST_JOB, result);
    }

    @Test
    void Gemini응답에_코드블록이_포함되어도_JSON을_추출한다() {
        when(chatModel.call(anyString()))
                .thenReturn("```json\n{\"actionType\":\"ADD_ROADMAP_ITEM\"}\n```");

        AiRoadmapChangeProposal.ActionType result = classifier.classify("자격증 추천해줘");

        assertEquals(AiRoadmapChangeProposal.ActionType.ADD_ROADMAP_ITEM, result);
    }

    @Test
    void Gemini의_구조화된_이동_명령을_복원한다() {
        when(chatModel.call(anyString())).thenReturn("""
                {"actionType":"MOVE_ITEM","targetRoadmapItemId":7,
                 "after":{"roadmapLane":"LEARNING","targetStage":"GRADE_4","displayOrder":20},
                 "reason":"4학년으로 이동"}
                """);

        AiRoadmapCommand command = classifier.command("7번 항목을 4학년으로 옮겨줘");

        assertEquals(AiRoadmapChangeProposal.ActionType.MOVE_ITEM, command.actionType());
        assertEquals(7L, command.targetRoadmapItemId());
        assertEquals("GRADE_4", command.after().targetStage().name());
        assertEquals("LEARNING", command.after().roadmapLane().name());
    }

    @Test
    void Gemini응답이_잘못되면_규칙_기반으로_fallback한다() {
        when(chatModel.call(anyString())).thenReturn("invalid response");

        assertEquals(AiRoadmapChangeProposal.ActionType.ADD_CUSTOM_ITEM,
                classifier.classify("프로젝트 추천해줘"));
    }

    @Test
    void Gemini응답의_actionType이_비어있으면_규칙_기반으로_fallback한다() {
        when(chatModel.call(anyString())).thenReturn("{\"actionType\":\"\"}");

        assertEquals(AiRoadmapChangeProposal.ActionType.ADD_CUSTOM_ITEM,
                classifier.classify("프로젝트 추천해줘"));
    }

    @Test
    void 빈_메시지는_Gemini를_호출하지_않고_NO_ACTION으로_처리한다() {
        AiRoadmapChangeProposal.ActionType result = classifier.classify("   ");

        assertEquals(AiRoadmapChangeProposal.ActionType.NO_ACTION, result);
    }
}
