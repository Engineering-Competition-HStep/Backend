package com.Hstep.Hstep.domain.airoadmap.exception;

import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.Hstep.Hstep.global.constant.StaticValue.*;

@Getter
@AllArgsConstructor
public enum AiRoadmapResponseCode implements BaseResponseCode {
    GRADE_RESTRICTED("AI_ROADMAP_403_1", FORBIDDEN, "개인 맞춤 AI 로드맵은 2학년부터 이용할 수 있습니다."),
    PROFILE_INCOMPLETE("AI_ROADMAP_403_2", FORBIDDEN, "학점과 개인 스펙 등록을 완료해주세요."),
    TRACK_REQUIRED("AI_ROADMAP_400_1", BAD_REQUEST, "소속 트랙 정보가 필요합니다."),
    UNSUPPORTED_ACTION("AI_ROADMAP_400_2", BAD_REQUEST, "현재 지원하지 않는 AI 로드맵 요청입니다."),
    INVALID_PROPOSAL_STATE("AI_ROADMAP_409_1", CONFLICT, "이미 처리된 변경 제안입니다."),
    AI_ROADMAP_NOT_FOUND("AI_ROADMAP_404_1", NOT_FOUND, "생성된 개인 AI 로드맵이 없습니다."),
    AI_ROADMAP_ITEM_NOT_FOUND("AI_ROADMAP_404_2", NOT_FOUND, "개인 로드맵 항목을 찾을 수 없습니다."),
    STANDARD_ITEM_NOT_FOUND("AI_ROADMAP_404_3", NOT_FOUND, "직무별 표준 로드맵 항목을 찾을 수 없습니다."),
    STANDARD_ROADMAP_NOT_FOUND("AI_ROADMAP_404_4", NOT_FOUND, "해당 직무의 로드맵이 아직 등록되지 않았습니다."),
    JOB_NOT_FOUND("AI_ROADMAP_404_5", NOT_FOUND, "직무를 찾을 수 없습니다."),
    RECOMMENDATION_NOT_FOUND("AI_ROADMAP_404_6", NOT_FOUND, "추천 가능한 직무가 없습니다."),
    PROPOSAL_NOT_FOUND("AI_ROADMAP_404_7", NOT_FOUND, "변경 제안을 찾을 수 없습니다."),

    ELIGIBILITY_GET_SUCCESS("AI_ROADMAP_200_1", OK, "AI 로드맵 이용 가능 여부를 조회했습니다."),
    PROFILE_REGISTRATION_SUCCESS("AI_ROADMAP_200_2", OK, "개인 스펙 등록 상태를 저장했습니다."),
    JOB_RECOMMENDATION_SUCCESS("AI_ROADMAP_200_3", OK, "관심 직무 추천 결과를 조회했습니다."),
    ROADMAP_GET_SUCCESS("AI_ROADMAP_200_4", OK, "개인 AI 로드맵을 조회했습니다."),
    ROADMAP_CREATE_SUCCESS("AI_ROADMAP_201_1", CREATED, "개인 AI 로드맵을 생성했습니다."),
    ROADMAP_ITEM_UPDATE_SUCCESS("AI_ROADMAP_200_5", OK, "개인 로드맵 항목을 변경했습니다."),
    CHAT_SUCCESS("AI_ROADMAP_200_6", OK, "AI 로드맵 요청을 처리했습니다."),
    PROPOSAL_APPLY_SUCCESS("AI_ROADMAP_200_7", OK, "로드맵 변경 제안을 반영했습니다."),
    PROPOSAL_CANCEL_SUCCESS("AI_ROADMAP_200_8", OK, "로드맵 변경 제안을 취소했습니다."),
    STANDARD_ITEM_MANAGE_SUCCESS("AI_ROADMAP_200_9", OK, "직무별 표준 로드맵 데이터를 처리했습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
