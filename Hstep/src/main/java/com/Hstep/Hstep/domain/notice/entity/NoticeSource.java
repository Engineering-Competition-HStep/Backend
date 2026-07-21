package com.Hstep.Hstep.domain.notice.entity;

public enum NoticeSource {

    HANSUNG_MAIN("한성대학교 전체 공지"),
    CSE("컴퓨터공학부 공지");

    private final String displayName;

    NoticeSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
