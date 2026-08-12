package com.Hstep.Hstep.domain.track.exception;

import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.Hstep.Hstep.global.constant.StaticValue.OK;

@Getter
@AllArgsConstructor
public enum TrackResponseCode implements BaseResponseCode {
    TRACK_LIST_GET_SUCCESS("TRACK_200_1", OK, "트랙 목록을 성공적으로 불러왔습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
