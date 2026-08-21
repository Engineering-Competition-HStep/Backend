package com.Hstep.Hstep.domain.auth.exception;

import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.Hstep.Hstep.global.constant.StaticValue.BAD_REQUEST;
import static com.Hstep.Hstep.global.constant.StaticValue.CONFLICT;
import static com.Hstep.Hstep.global.constant.StaticValue.CREATED;
import static com.Hstep.Hstep.global.constant.StaticValue.INTERNAL_SERVER_ERROR;
import static com.Hstep.Hstep.global.constant.StaticValue.OK;
import static com.Hstep.Hstep.global.constant.StaticValue.UNAUTHORIZED;

@Getter
@AllArgsConstructor
public enum AuthResponseCode implements BaseResponseCode {
    INVALID_TRACKS("AUTH_400_1", BAD_REQUEST, "트랙은 중복 없이 1개 이상 2개 이하로 선택해야 합니다."),
    CURRENT_PASSWORD_MISMATCH("AUTH_400_2", BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    SAME_PASSWORD("AUTH_400_3", BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    EMAIL_VERIFICATION_REQUIRED("AUTH_400_4", BAD_REQUEST, "학교 이메일 인증을 먼저 완료해주세요."),
    EMAIL_VERIFICATION_TOKEN_INVALID("AUTH_400_5", BAD_REQUEST, "유효하지 않은 이메일 인증 링크입니다."),
    EMAIL_VERIFICATION_EXPIRED("AUTH_400_6", BAD_REQUEST, "이메일 인증 링크가 만료되었습니다. 인증 메일을 다시 요청해주세요."),
    INVALID_CREDENTIALS("AUTH_401_1", UNAUTHORIZED, "학번 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN("AUTH_401_2", UNAUTHORIZED, "유효하지 않거나 만료된 인증 토큰입니다."),
    USER_ID_DUPLICATION("AUTH_409_1", CONFLICT, "이미 가입된 학번입니다."),
    EMAIL_DUPLICATION("AUTH_409_2", CONFLICT, "이미 사용 중인 이메일입니다."),
    EMAIL_SEND_FAILED("AUTH_500_1", INTERNAL_SERVER_ERROR, "인증 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요."),

    SIGNUP_SUCCESS("AUTH_201_1", CREATED, "회원가입이 완료되었습니다."),
    LOGIN_SUCCESS("AUTH_200_1", OK, "로그인에 성공했습니다."),
    USER_ID_AVAILABLE("AUTH_200_2", OK, "사용할 수 있는 학번입니다."),
    EMAIL_AVAILABLE("AUTH_200_3", OK, "사용할 수 있는 이메일입니다."),
    PASSWORD_CHANGE_SUCCESS("AUTH_200_4", OK, "비밀번호가 변경되었습니다."),
    EMAIL_VERIFICATION_SENT("AUTH_200_5", OK, "학교 이메일로 인증 링크를 발송했습니다."),
    EMAIL_VERIFICATION_SUCCESS("AUTH_200_6", OK, "학교 이메일 인증이 완료되었습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
