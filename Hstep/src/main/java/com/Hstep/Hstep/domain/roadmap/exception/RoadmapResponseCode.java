package com.Hstep.Hstep.domain.roadmap.exception;

import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.Hstep.Hstep.global.constant.StaticValue.CONFLICT;
import static com.Hstep.Hstep.global.constant.StaticValue.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum RoadmapResponseCode implements BaseResponseCode {
    ROADMAP_NOT_FOUND("ROADMAP_404_1", NOT_FOUND, "존재하지 않는 기본 로드맵입니다."),
    ROADMAP_ITEM_NOT_FOUND("ROADMAP_404_2", NOT_FOUND, "존재하지 않는 로드맵 항목입니다."),
    TRACK_NOT_FOUND("ROADMAP_404_3", NOT_FOUND, "존재하지 않는 트랙입니다."),
    DUPLICATE_ROADMAP_FOR_TRACK("ROADMAP_409_1", CONFLICT, "이미 해당 트랙에 등록된 기본 로드맵이 있습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}