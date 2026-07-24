package com.Hstep.Hstep.domain.notice.crawler;

import com.Hstep.Hstep.domain.notice.entity.NoticeSource;

import java.util.List;

public enum NoticeBoard {

    HANSUNG_MAIN(
            NoticeSource.HANSUNG_MAIN,
            "https://www.hansung.ac.kr/bbs/hansung/2127/artclList.do",
            List.of(
                    "Stay & Study in school 장학사업",
                    "강소기업채용",
                    "교육프로그램",
                    "채용정보",
                    "교외장학금",
                    "국가장학금",
                    "국가근로",
                    "현장실습",
                    "한성공지",
                    "비교과",
                    "학사",
                    "진로",
                    "취업",
                    "장학",
                    "창업",
                    "국제",
                    "감염병",
                    "기타"
            )
    ),

    CSE(
            NoticeSource.CSE,
            "https://hansung.ac.kr/bbs/CSE/1974/artclList.do",
            List.of(
                    "취업 및 연수",
                    "공지사항",
                    "기타"
            )
    );

    private final NoticeSource source;
    private final String listUrl;
    private final List<String> categories;

    NoticeBoard(
            NoticeSource source,
            String listUrl,
            List<String> categories
    ) {
        this.source = source;
        this.listUrl = listUrl;
        this.categories = categories;
    }

    public NoticeSource getSource() {
        return source;
    }

    public String getListUrl() {
        return listUrl;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String pageUrl(int page) {
        return listUrl + "?page=" + page;
    }
}
