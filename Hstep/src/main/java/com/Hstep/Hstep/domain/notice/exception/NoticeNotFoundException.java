package com.Hstep.Hstep.domain.notice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoticeNotFoundException extends RuntimeException {

    public NoticeNotFoundException(Long noticeId) {
        super("공지사항을 찾을 수 없습니다. noticeId=" + noticeId);
    }
}
