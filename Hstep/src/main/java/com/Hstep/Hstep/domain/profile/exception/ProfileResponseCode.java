package com.Hstep.Hstep.domain.profile.exception;

import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.Hstep.Hstep.global.constant.StaticValue.BAD_REQUEST;
import static com.Hstep.Hstep.global.constant.StaticValue.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum ProfileResponseCode implements BaseResponseCode {
    INVALID_GRADE("PROFILE_400_1", BAD_REQUEST, "현재 학년보다 높은 학년의 학점은 입력할 수 없습니다."),
    CERTIFICATE_NOT_FOUND("PROFILE_404_1", NOT_FOUND, "존재하지 않는 자격증입니다."),
    AWARD_NOT_FOUND("PROFILE_404_2", NOT_FOUND, "존재하지 않는 수상경력입니다."),
    VOLUNTEER_NOT_FOUND("PROFILE_404_3", NOT_FOUND, "존재하지 않는 봉사활동입니다."),
    EXTRA_ACTIVITY_NOT_FOUND("PROFILE_404_4", NOT_FOUND, "존재하지 않는 활동입니다."),
    USER_GRADE_GPA_NOT_FOUND("PROFILE_404_5", NOT_FOUND, "존재하지 않는 학년별 평균 학점입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}