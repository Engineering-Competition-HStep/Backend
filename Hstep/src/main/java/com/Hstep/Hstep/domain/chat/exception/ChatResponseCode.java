package com.Hstep.Hstep.domain.chat.exception;

import com.Hstep.Hstep.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.Hstep.Hstep.global.constant.StaticValue.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum ChatResponseCode implements BaseResponseCode {
    CHAT_ROOM_NOT_FOUND("CHAT_404_1", NOT_FOUND, "존재하지 않는 대화방입니다.");

    private final String code;
    private final int httpStatus;
    private final String message;
}