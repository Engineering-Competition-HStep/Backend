package com.Hstep.Hstep.domain.chat.prompt;

public class ChatContextBuilder {

    public static String build(
            String track1, String track2,
            Integer grade, String overallGpa,
            String certificates, String awards,
            String volunteers, String activities,
            String roadmapTemplate
    ) {
        return """
                [학생 정보]
                - 1트랙: %s
                - 2트랙: %s
                - 학년: %d학년
                - 평균학점: %s

                [보유 스펙]
                - 자격증: %s
                - 수상경력: %s
                - 자원봉사: %s
                - 기타활동: %s

                [트랙 기준 추천 로드맵]
                %s

                위 정보를 참고해서 답변해줘.
                """.formatted(
                track1, track2 != null ? track2 : "없음",
                grade, overallGpa != null ? overallGpa : "미입력",
                certificates, awards, volunteers, activities,
                roadmapTemplate
        );
    }
}