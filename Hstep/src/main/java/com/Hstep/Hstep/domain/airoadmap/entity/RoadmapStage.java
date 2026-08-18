package com.Hstep.Hstep.domain.airoadmap.entity;

import java.util.Arrays;

public enum RoadmapStage {
    GRADE_2(2),
    GRADE_3(3),
    GRADE_4(4),
    JOB_SEEKER(5);

    private final int grade;

    RoadmapStage(int grade) {
        this.grade = grade;
    }

    public int getGrade() {
        return grade;
    }

    public static RoadmapStage fromGrade(Integer grade) {
        if (grade == null) return null;
        return Arrays.stream(values())
                .filter(stage -> stage.grade == grade)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 로드맵 학년입니다: " + grade));
    }
}
