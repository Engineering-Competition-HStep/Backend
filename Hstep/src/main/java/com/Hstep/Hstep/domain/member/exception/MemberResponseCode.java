package com.Hstep.Hstep.domain.member.exception;

import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.Hstep.Hstep.global.constant.StaticValue.BAD_REQUEST;
import static com.Hstep.Hstep.global.constant.StaticValue.NOT_FOUND;
import static com.Hstep.Hstep.global.constant.StaticValue.OK;

@Getter
@AllArgsConstructor
public enum MemberResponseCode implements BaseResponseCode {
    INVALID_TRACKS("MEMBER_400_1", BAD_REQUEST, "트랙은 중복 없이 1개 이상 2개 이하로 선택해야 합니다."),
    MEMBER_NOT_FOUND("MEMBER_404_1", NOT_FOUND, "회원을 찾을 수 없습니다."),

    MEMBER_GET_SUCCESS("MEMBER_200_1", OK, "회원 정보를 성공적으로 불러왔습니다."),
    MEMBER_UPDATE_SUCCESS("MEMBER_200_2", OK, "회원 정보를 성공적으로 수정했습니다."),
    MEMBER_DELETE_SUCCESS("MEMBER_200_3", OK, "회원 탈퇴가 완료되었습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}
