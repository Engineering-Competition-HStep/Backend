package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;
import com.Hstep.Hstep.global.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void Gemini응답이_잘못되면_BaseException을_발생시킨다() {
        when(chatModel.call(anyString())).thenReturn("invalid response");

        assertThrows(BaseException.class, () -> classifier.classify("프로젝트 추천해줘"));
    }

    @Test
    void Gemini응답의_actionType이_비어있으면_BaseException을_발생시킨다() {
        when(chatModel.call(anyString())).thenReturn("{\"actionType\":\"\"}");

        assertThrows(BaseException.class, () -> classifier.classify("프로젝트 추천해줘"));
    }

    @Test
    void 빈_메시지는_Gemini를_호출하지_않고_NO_ACTION으로_처리한다() {
        AiRoadmapChangeProposal.ActionType result = classifier.classify("   ");

        assertEquals(AiRoadmapChangeProposal.ActionType.NO_ACTION, result);
    }
}
