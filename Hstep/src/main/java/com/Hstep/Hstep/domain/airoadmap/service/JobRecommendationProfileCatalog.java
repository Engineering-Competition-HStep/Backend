package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class JobRecommendationProfileCatalog {

    public JobSkillProfile resolve(Job job) {
        Set<String> skillKeywords = new LinkedHashSet<>(baseKeywords(job.getJobCategory()));
        String normalized = normalize(job.getJobName());
        if (normalized.contains("백엔드")) skillKeywords.addAll(Set.of("java", "spring", "sql", "jpa", "api", "서버"));
        if (normalized.contains("프론트")) skillKeywords.addAll(Set.of("javascript", "typescript", "react", "웹"));
        if (normalized.contains("데이터")) skillKeywords.addAll(Set.of("python", "sql", "통계", "데이터"));
        if (normalized.contains("보안")) skillKeywords.addAll(Set.of("보안", "네트워크", "linux", "암호"));
        return new JobSkillProfile(skillKeywords, courseKeywords(job.getJobCategory()), familyOf(job.getJobName()));
    }

    private Set<String> baseKeywords(JobCategory category) {
        return switch (category) {
            case SOFTWARE -> Set.of("java", "python", "javascript", "spring", "react", "sql", "git", "api", "개발");
            case DATA_AI -> Set.of("python", "sql", "통계", "데이터", "인공지능", "머신러닝");
            case SECURITY -> Set.of("보안", "네트워크", "linux", "암호", "취약점", "로그");
            case DESIGN_MEDIA -> Set.of("디자인", "영상", "그래픽", "ui", "ux", "콘텐츠");
            case BUSINESS_TRADE, FINANCE_ACCOUNTING -> Set.of("경영", "회계", "재무", "마케팅", "무역", "분석");
            default -> words(category.getDisplayName());
        };
    }

    private Set<String> courseKeywords(JobCategory category) {
        return switch (category) {
            case SOFTWARE -> Set.of("자료구조", "알고리즘", "운영체제", "네트워크", "데이터베이스");
            case DATA_AI -> Set.of("통계", "선형대수", "데이터베이스", "알고리즘");
            case SECURITY -> Set.of("네트워크", "운영체제", "암호", "보안");
            default -> baseKeywords(category);
        };
    }

    String familyOf(String jobName) {
        String value = normalize(jobName);
        if (value.contains("백엔드") || value.contains("서버")) return "BACKEND";
        if (value.contains("프론트") || value.contains("웹퍼블")) return "FRONTEND";
        if (value.contains("모바일") || value.contains("안드로이드") || value.contains("ios")) return "MOBILE";
        if (value.contains("데이터")) return "DATA";
        if (value.contains("ai") || value.contains("인공지능") || value.contains("머신러닝")) return "AI";
        if (value.contains("클라우드") || value.contains("devops")) return "CLOUD";
        if (value.contains("보안")) return "SECURITY";
        return "OTHER:" + value;
    }

    private Set<String> words(String value) {
        return new LinkedHashSet<>(Arrays.asList(value.toLowerCase(Locale.ROOT).split("[·\\s]+")));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    public record JobSkillProfile(Set<String> skillKeywords, Set<String> courseKeywords, String family) {}
}
