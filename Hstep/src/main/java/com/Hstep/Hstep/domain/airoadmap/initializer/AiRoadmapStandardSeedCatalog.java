package com.Hstep.Hstep.domain.airoadmap.initializer;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapItemType;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapLane;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapStage;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class AiRoadmapStandardSeedCatalog {

    public static final int TEMPLATE_VERSION = 2;

    private static final int TITLE_MAX_LENGTH = 120;
    private static final int DESCRIPTION_MAX_LENGTH = 1000;
    private static final int KEYWORD_MAX_LENGTH = 300;
    private static final int REASON_MAX_LENGTH = 500;

    private AiRoadmapStandardSeedCatalog() {
    }

    public static List<StandardItemSeed> createFor(Job job) {
        Objects.requireNonNull(job, "job은 필수입니다.");
        return createFor(job.getJobName(), job.getJobCategory());
    }

    static List<StandardItemSeed> createFor(String jobName, JobCategory category) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("직무명은 비어 있을 수 없습니다.");
        }
        Objects.requireNonNull(category, "직무 카테고리는 필수입니다.");

        String normalizedJobName = jobName.trim();
        if (category == JobCategory.SOFTWARE) {
            return createSoftwareProfile(normalizedJobName);
        }
        DomainProfile profile = profileOf(category);
        String foundation = firstClause(profile.foundation());
        String tools = firstClause(profile.tools());
        String practice = firstClause(profile.practice());
        String project = firstClause(profile.projectTheme());
        String credential = firstClause(profile.credentialGuide());
        String fieldExperience = firstClause(profile.fieldExperience());
        String portfolio = firstClause(profile.portfolioEvidence());
        String interview = firstClause(profile.interviewFocus());

        return List.of(
                seed(
                        "G2_FOUNDATION",
                        AiRoadmapStandardItem.Category.COURSE,
                        2,
                        AiRoadmapStandardItem.Priority.HIGH,
                        10,
                        foundation + "와 " + tools + " 실습",
                        normalizedJobName + " 직무에 필요한 " + profile.foundation()
                                + "를 학습하고, " + profile.tools() + "을 활용하는 기초 실습을 진행합니다.",
                        keywords(normalizedJobName, category.getDisplayName(), profile.foundation(), profile.tools()),
                        "3학년 심화 프로젝트와 실무 경험을 준비하기 위한 기본 학습 항목입니다.",
                        true
                ),
                seed(
                        "G2_ROLE_RESEARCH",
                        AiRoadmapStandardItem.Category.ETC,
                        2,
                        AiRoadmapStandardItem.Priority.HIGH,
                        20,
                        normalizedJobName + " 주요 업무와 채용 요구 분석",
                        normalizedJobName + "의 주요 업무, 협업 대상, 진입 경로와 채용 공고의 공통 요구 역량을 조사해 비교표로 정리합니다.",
                        keywords(normalizedJobName, "직무분석", "산업분석", "채용공고", "현직자 인터뷰"),
                        "직무의 실제 역할을 이해하고 이후 활동의 우선순위를 정하기 위한 항목입니다.",
                        true
                ),
                seed(
                        "G2_MINI_PROJECT",
                        AiRoadmapStandardItem.Category.PROJECT,
                        2,
                        AiRoadmapStandardItem.Priority.MEDIUM,
                        30,
                        project + " 미니 결과물 제작",
                        profile.practice() + "을 적용하여 " + profile.projectTheme()
                                + " 주제의 작은 결과물을 완성하고 피드백을 반영합니다.",
                        keywords(normalizedJobName, "미니 프로젝트", profile.practice(), profile.tools()),
                        "기초 지식을 실제 결과물로 연결하고 직무 적합성을 탐색하기 위한 선택 항목입니다.",
                        false
                ),
                seed(
                        "G3_CORE_SKILL",
                        AiRoadmapStandardItem.Category.COURSE,
                        3,
                        AiRoadmapStandardItem.Priority.HIGH,
                        10,
                        practice + " 심화 실습",
                        profile.foundation() + "와 " + profile.practice()
                                + "을 실제 과제에 적용할 수 있도록 심화 학습과 반복 실습을 진행합니다.",
                        keywords(normalizedJobName, "핵심 역량", profile.foundation(), profile.practice(), profile.tools()),
                        "대표 프로젝트와 실무 경험을 수행하기 전에 반드시 강화해야 할 핵심 역량입니다.",
                        true
                ),
                seed(
                        "G3_PORTFOLIO_PROJECT",
                        AiRoadmapStandardItem.Category.PROJECT,
                        3,
                        AiRoadmapStandardItem.Priority.HIGH,
                        20,
                        project + " 대표 결과물 제작",
                        profile.practice() + "을 바탕으로 " + profile.projectTheme()
                                + "를 기획·수행하고, " + profile.portfolioEvidence() + " 형태로 결과와 개선 과정을 기록합니다.",
                        keywords(normalizedJobName, "포트폴리오", "대표 프로젝트", profile.projectTheme(), profile.tools()),
                        "직무 역량과 문제 해결 과정을 구체적인 결과물로 증명하기 위한 필수 항목입니다.",
                        true
                ),
                seed(
                        "G3_SPECIALIZED_PROJECT",
                        AiRoadmapStandardItem.Category.PROJECT,
                        3,
                        AiRoadmapStandardItem.Priority.MEDIUM,
                        30,
                        project + " 협업·품질 검증 확장",
                        "대표 프로젝트에 협업, 사용자·이해관계자 요구, 품질 검증, 성과 측정 요소를 추가하여 "
                                + profile.projectTheme() + "의 완성도를 높입니다.",
                        keywords(normalizedJobName, "심화 프로젝트", "협업", "품질 검증", "성과 측정", profile.projectTheme()),
                        "기본 프로젝트를 넘어 실무에 가까운 복잡도와 협업 경험을 확보하기 위한 선택 항목입니다.",
                        false
                ),
                seed(
                        "G3_CERTIFICATE",
                        AiRoadmapStandardItem.Category.CERTIFICATE,
                        3,
                        AiRoadmapStandardItem.Priority.MEDIUM,
                        40,
                        credential + " 중 목표 자격 선택",
                        profile.credentialGuide()
                                + "을 참고하여 현재 수준과 목표 직무에 적합한 자격·인증 또는 교육 과정을 하나 선택해 준비합니다.",
                        keywords(normalizedJobName, "자격증", "인증", "교육 이수", profile.credentialGuide()),
                        "직무 지식을 체계적으로 점검하고 부족한 이론을 보완하기 위한 선택 항목입니다.",
                        false
                ),
                seed(
                        "G3_FIELD_EXPERIENCE",
                        AiRoadmapStandardItem.Category.INTERNSHIP,
                        3,
                        AiRoadmapStandardItem.Priority.HIGH,
                        50,
                        fieldExperience + " 지원 준비",
                        profile.fieldExperience()
                                + "과 관련된 인턴, 현장실습, 산학 프로젝트 또는 실무형 활동을 탐색하고 지원 자료를 준비합니다.",
                        keywords(normalizedJobName, "인턴", "현장실습", "산학 프로젝트", profile.fieldExperience()),
                        "실제 조직의 업무 방식과 협업 과정을 경험하기 위한 핵심 준비 항목입니다.",
                        true
                ),
                seed(
                        "G3_EXTERNAL_ACTIVITY",
                        AiRoadmapStandardItem.Category.CONTEST,
                        3,
                        AiRoadmapStandardItem.Priority.LOW,
                        60,
                        practice + " 외부 협업 참여",
                        profile.practice()
                                + " 역량을 검증할 수 있는 공모전, 대외활동 또는 학술·산업 프로젝트에 참여해 외부 피드백과 협업 경험을 확보합니다.",
                        keywords(normalizedJobName, "공모전", "대외활동", "협업", "외부 피드백", profile.practice()),
                        "외부 평가와 협업 경험을 추가로 확보하기 위한 선택 항목입니다.",
                        false
                ),
                seed(
                        "G4_PORTFOLIO_REFINEMENT",
                        AiRoadmapStandardItem.Category.PROJECT,
                        4,
                        AiRoadmapStandardItem.Priority.HIGH,
                        10,
                        portfolio + " 포트폴리오 정리",
                        profile.portfolioEvidence()
                                + "를 중심으로 대표 결과물을 선별하고, 역할·문제·과정·결과·회고가 드러나도록 포트폴리오를 보완합니다.",
                        keywords(normalizedJobName, "포트폴리오", "문제 해결", "성과", "회고", profile.portfolioEvidence()),
                        "채용 담당자가 직무 역량과 기여도를 빠르게 확인할 수 있도록 결과물을 정리하는 필수 항목입니다.",
                        true
                ),
                seed(
                        "G4_APPLICATION",
                        AiRoadmapStandardItem.Category.ETC,
                        4,
                        AiRoadmapStandardItem.Priority.HIGH,
                        20,
                        normalizedJobName + " 공고 요구 역량과 지원서 연결",
                        "채용 공고별 요구 역량을 분석하여 보유 경험과 연결하고, "
                                + normalizedJobName + " 지원서와 포트폴리오 버전을 공고별로 관리합니다.",
                        keywords(normalizedJobName, "채용 공고", "지원서", "자기소개서", "경험 정리", "포트폴리오"),
                        "지원하는 조직과 공고에 맞춰 경험을 구체적으로 제시하기 위한 필수 항목입니다.",
                        true
                ),
                seed(
                        "G4_INTERVIEW",
                        AiRoadmapStandardItem.Category.ETC,
                        4,
                        AiRoadmapStandardItem.Priority.HIGH,
                        30,
                        interview + " 면접·실무 과제 연습",
                        profile.interviewFocus()
                                + "을 중심으로 예상 질문, 경험 기반 답변, 실무 과제와 발표를 반복 연습합니다.",
                        keywords(normalizedJobName, "면접", "실무 과제", "발표", "경험 기반 답변", profile.interviewFocus()),
                        "직무 지식과 프로젝트 경험을 제한된 시간 안에 논리적으로 설명하기 위한 필수 항목입니다.",
                        true
                )
        );
    }

    private static List<StandardItemSeed> createSoftwareProfile(String jobName) {
        return List.of(
                software("G2_FOUNDATION", RoadmapLane.LEARNING, RoadmapItemType.PROGRAMMING_LANGUAGE, RoadmapStage.GRADE_2, 10,
                        "Java 기본 문법과 객체지향 프로그래밍", "클래스·상속·인터페이스를 사용한 콘솔 프로그램을 구현하고 README에 설계를 설명합니다.", true, true, "Java, 객체지향, OOP"),
                software("G2_ROLE_RESEARCH", RoadmapLane.LEARNING, RoadmapItemType.CS_SUBJECT, RoadmapStage.GRADE_2, 20,
                        "자료구조·알고리즘 기초", "배열·리스트·스택·큐·트리를 구현하고 시간 복잡도를 설명할 수 있도록 연습합니다.", true, true, "자료구조, 알고리즘, 시간복잡도"),
                software("G2_MINI_PROJECT", RoadmapLane.LEARNING, RoadmapItemType.CS_SUBJECT, RoadmapStage.GRADE_2, 30,
                        "관계형 데이터베이스와 SQL", "테이블 관계를 설계하고 JOIN·집계·서브쿼리를 포함한 SQL을 작성합니다.", true, true, "SQL, 데이터베이스, RDB"),
                software("G3_CORE_SKILL", RoadmapLane.LEARNING, RoadmapItemType.DEVELOPMENT_TOOL, RoadmapStage.GRADE_2, 40,
                        "Git·GitHub 버전 관리와 협업", "브랜치·커밋·Pull Request 흐름으로 협업하고 충돌 해결 과정을 기록합니다.", true, true, "Git, GitHub, 협업"),
                software("G3_PORTFOLIO_PROJECT", RoadmapLane.PROJECT, RoadmapItemType.MINI_PROJECT, RoadmapStage.GRADE_2, 50,
                        "Spring Boot REST API 미니 프로젝트", "Controller-Service-Repository 구조로 CRUD와 예외 응답을 구현하고 API 명세를 작성합니다.", true, true, "Spring Boot, REST API, CRUD"),
                software("G3_SPECIALIZED_PROJECT", RoadmapLane.CERTIFICATION, RoadmapItemType.CERTIFICATE, RoadmapStage.GRADE_2, 60,
                        "SQLD 준비", "등록 자격증명으로만 완료 판정하며 SQL 기본 이론과 문제 풀이 계획을 세웁니다.", false, true, "SQLD"),
                software("G3_CERTIFICATE", RoadmapLane.EXPERIENCE, RoadmapItemType.HACKATHON, RoadmapStage.GRADE_2, 70,
                        "개발 동아리 또는 교내 해커톤 참여", "팀 역할을 맡아 결과물을 배포하고 기여 내용과 회고를 남깁니다.", false, true, "개발동아리, 해커톤"),
                software("G3_FIELD_EXPERIENCE", RoadmapLane.LEARNING, RoadmapItemType.FRAMEWORK, RoadmapStage.GRADE_3, 10,
                        "Spring Boot 계층형 아키텍처", "계층별 책임과 의존 방향을 지킨 API를 구현하고 예외 처리 정책을 적용합니다.", true, true, "Spring Boot, 계층형 아키텍처"),
                software("G3_EXTERNAL_ACTIVITY", RoadmapLane.LEARNING, RoadmapItemType.FRAMEWORK, RoadmapStage.GRADE_3, 20,
                        "JPA 연관관계와 트랜잭션", "연관관계 매핑·지연 로딩·트랜잭션 경계를 적용하고 쿼리 문제를 점검합니다.", true, true, "JPA, 트랜잭션, 연관관계"),
                software("G4_PORTFOLIO_REFINEMENT", RoadmapLane.LEARNING, RoadmapItemType.CS_SUBJECT, RoadmapStage.GRADE_3, 30,
                        "HTTP·REST·네트워크 기초", "HTTP 메서드·상태 코드·캐시와 TCP/IP 흐름을 API 동작과 연결해 설명합니다.", true, true, "HTTP, REST, 네트워크"),
                software("G4_APPLICATION", RoadmapLane.LEARNING, RoadmapItemType.CS_SUBJECT, RoadmapStage.GRADE_3, 40,
                        "운영체제와 동시성 기초", "프로세스·스레드·락과 동시성 문제를 Java 예제로 재현하고 해결합니다.", true, true, "운영체제, 동시성, 스레드"),
                software("G4_INTERVIEW", RoadmapLane.LEARNING, RoadmapItemType.DEVELOPMENT_TOOL, RoadmapStage.GRADE_3, 50,
                        "JUnit 단위·통합 테스트", "핵심 서비스 단위 테스트와 API 통합 테스트를 작성하고 실패 경로를 검증합니다.", true, true, "JUnit, 테스트, 통합테스트"),
                software("G3_TEAM_AUTH_PROJECT", RoadmapLane.PROJECT, RoadmapItemType.TEAM_PROJECT, RoadmapStage.GRADE_3, 60,
                        "인증·인가가 포함된 팀 백엔드 프로젝트", "역할 분담으로 인증·인가와 핵심 API를 구현하고 코드 리뷰와 배포 기록을 남깁니다.", true, true, "인증, 인가, 팀프로젝트"),
                software("G3_DOCKER_CICD", RoadmapLane.LEARNING, RoadmapItemType.DEVELOPMENT_TOOL, RoadmapStage.GRADE_3, 70,
                        "Docker 배포와 CI/CD", "Docker 이미지로 서비스를 실행하고 테스트·빌드·배포 자동화 파이프라인을 구성합니다.", true, true, "Docker, CI/CD, 배포"),
                software("G3_ENGINEER_CERT", RoadmapLane.CERTIFICATION, RoadmapItemType.CERTIFICATE, RoadmapStage.GRADE_3, 80,
                        "정보처리기사 준비", "등록 자격증명으로만 완료 판정하며 시험 범위별 학습과 문제 풀이를 진행합니다.", false, true, "정보처리기사"),
                software("G3_EXTERNAL_BUILD", RoadmapLane.EXPERIENCE, RoadmapItemType.OPEN_SOURCE, RoadmapStage.GRADE_3, 90,
                        "해커톤·산학 프로젝트·오픈소스 참여", "외부 협업에서 이슈·PR·발표 또는 배포 결과로 기여를 증명합니다.", false, true, "해커톤, 산학프로젝트, 오픈소스"),
                software("G4_REDIS", RoadmapLane.LEARNING, RoadmapItemType.FRAMEWORK, RoadmapStage.GRADE_4, 10,
                        "Redis 캐시와 성능 최적화", "캐시 적용 전후 지표를 비교하고 일관성·만료 정책과 병목 개선 근거를 기록합니다.", true, true, "Redis, 캐시, 성능"),
                software("G4_OBSERVABILITY", RoadmapLane.LEARNING, RoadmapItemType.DEVELOPMENT_TOOL, RoadmapStage.GRADE_4, 20,
                        "보안·로깅·모니터링", "입력 검증·권한·구조화 로그·핵심 지표를 적용하고 장애 확인 절차를 문서화합니다.", true, true, "보안, 로깅, 모니터링"),
                software("G4_SYSTEM_DESIGN", RoadmapLane.LEARNING, RoadmapItemType.CS_SUBJECT, RoadmapStage.GRADE_4, 30,
                        "확장 가능한 서버와 시스템 설계", "요구량을 가정하고 데이터 저장·캐시·비동기 처리·장애 대응 설계를 설명합니다.", true, true, "시스템설계, 확장성, 서버"),
                software("G4_PRODUCTION_PORTFOLIO", RoadmapLane.PROJECT, RoadmapItemType.PORTFOLIO_PROJECT, RoadmapStage.GRADE_4, 40,
                        "운영 환경을 고려한 백엔드 포트폴리오", "배포·보안·관측·복구를 포함한 서비스를 운영하고 의사결정과 개선 결과를 정리합니다.", true, true, "포트폴리오, 운영, 배포"),
                software("G4_LOAD_TEST", RoadmapLane.PROJECT, RoadmapItemType.PORTFOLIO_PROJECT, RoadmapStage.GRADE_4, 50,
                        "부하 테스트와 병목 구간 개선", "재현 가능한 부하 시나리오로 병목을 찾고 개선 전후 응답 시간과 처리량을 비교합니다.", false, true, "부하테스트, 병목, 성능"),
                software("G4_CLOUD_CERT", RoadmapLane.CERTIFICATION, RoadmapItemType.VENDOR_CERTIFICATION, RoadmapStage.GRADE_4, 60,
                        "클라우드 관련 인증 준비", "목표 인증을 직접 선택하고 학습 범위와 실습 기록을 관리합니다.", false, false, "클라우드, 인증"),
                software("G4_INTERNSHIP", RoadmapLane.EXPERIENCE, RoadmapItemType.INTERNSHIP, RoadmapStage.GRADE_4, 70,
                        "백엔드 인턴·현장실습 지원", "직무 공고를 분석해 이력서와 포트폴리오를 맞추고 지원 결과를 기록합니다.", true, true, "백엔드, 인턴, 현장실습"),
                software("JS_CODING_TEST", RoadmapLane.EXPERIENCE, RoadmapItemType.JOB_PREPARATION, RoadmapStage.JOB_SEEKER, 10,
                        "코딩 테스트와 알고리즘 문제 풀이", "주간 문제 풀이 목표를 세우고 오답 원인과 시간 복잡도를 기록합니다.", true, true, "코딩테스트, 알고리즘"),
                software("JS_TECH_INTERVIEW", RoadmapLane.EXPERIENCE, RoadmapItemType.JOB_PREPARATION, RoadmapStage.JOB_SEEKER, 20,
                        "CS·백엔드 기술 면접 준비", "CS와 프로젝트 질문에 근거·선택·결과가 드러나는 답변을 반복 연습합니다.", true, true, "CS, 백엔드, 기술면접"),
                software("JS_GITHUB_DOCS", RoadmapLane.PROJECT, RoadmapItemType.PORTFOLIO_PROJECT, RoadmapStage.JOB_SEEKER, 30,
                        "GitHub·README·API 문서 정리", "대표 저장소의 실행 방법·구조·API·기여·트러블슈팅을 빠짐없이 정리합니다.", true, true, "GitHub, README, API문서"),
                software("JS_TROUBLESHOOTING", RoadmapLane.PROJECT, RoadmapItemType.PORTFOLIO_PROJECT, RoadmapStage.JOB_SEEKER, 40,
                        "오류 해결·성능 개선 경험 정리", "문제·원인·대안·검증 지표가 드러나는 기술 사례를 작성합니다.", true, true, "오류해결, 성능개선"),
                software("JS_POSTING_ANALYSIS", RoadmapLane.EXPERIENCE, RoadmapItemType.JOB_PREPARATION, RoadmapStage.JOB_SEEKER, 50,
                        "채용 공고별 요구 기술 분석", "지원 공고의 필수·우대 기술을 보유 경험과 연결하고 부족한 항목을 표시합니다.", true, true, "채용공고, 요구기술"),
                software("JS_APPLICATION_INTERVIEW", RoadmapLane.EXPERIENCE, RoadmapItemType.JOB_PREPARATION, RoadmapStage.JOB_SEEKER, 60,
                        "이력서·자기소개서·모의 면접", "공고별 문서를 작성하고 모의 면접 피드백을 반영해 답변을 개선합니다.", true, true, "이력서, 자기소개서, 모의면접"),
                software("JS_APPLY", RoadmapLane.EXPERIENCE, RoadmapItemType.JOB_PREPARATION, RoadmapStage.JOB_SEEKER, 70,
                        jobName + " 신입·인턴 공고 지원", "지원 일정·전형 결과·회고를 기록하고 다음 지원 문서에 반영합니다.", true, true, "신입, 인턴, 지원")
        );
    }

    private static StandardItemSeed software(String key, RoadmapLane lane, RoadmapItemType type,
                                              RoadmapStage stage, int order, String title, String description,
                                              boolean core, boolean included, String keyword) {
        AiRoadmapStandardItem.Category category = switch (lane) {
            case LEARNING -> AiRoadmapStandardItem.Category.COURSE;
            case PROJECT -> AiRoadmapStandardItem.Category.PROJECT;
            case CERTIFICATION -> AiRoadmapStandardItem.Category.CERTIFICATE;
            case EXPERIENCE -> type == RoadmapItemType.INTERNSHIP
                    ? AiRoadmapStandardItem.Category.INTERNSHIP
                    : type == RoadmapItemType.HACKATHON || type == RoadmapItemType.CONTEST
                    ? AiRoadmapStandardItem.Category.CONTEST : AiRoadmapStandardItem.Category.ETC;
        };
        AiRoadmapStandardItem.Priority priority = core
                ? AiRoadmapStandardItem.Priority.HIGH : AiRoadmapStandardItem.Priority.MEDIUM;
        return new StandardItemSeed(key, category, stage.getGrade(), priority, order,
                limit(title, TITLE_MAX_LENGTH), limit(description, DESCRIPTION_MAX_LENGTH),
                limit(keyword, KEYWORD_MAX_LENGTH), "개인 스펙 증거를 보수적으로 반영하며 사용자가 직접 완료 상태를 조정할 수 있습니다.",
                null, included, lane, type, stage, core, included, TEMPLATE_VERSION);
    }

    private static StandardItemSeed seed(
            String seedKey,
            AiRoadmapStandardItem.Category category,
            int targetGrade,
            AiRoadmapStandardItem.Priority priority,
            int displayOrder,
            String title,
            String description,
            String keyword,
            String recommendationReason,
            boolean requiredItem
    ) {
        return new StandardItemSeed(
                seedKey,
                category,
                targetGrade,
                priority,
                displayOrder,
                limit(title, TITLE_MAX_LENGTH),
                limit(description, DESCRIPTION_MAX_LENGTH),
                limit(keyword, KEYWORD_MAX_LENGTH),
                limit(recommendationReason, REASON_MAX_LENGTH),
                null,
                requiredItem,
                AiRoadmapStandardItem.inferLane(category),
                inferItemType(category, title),
                RoadmapStage.fromGrade(targetGrade),
                requiredItem,
                requiredItem,
                TEMPLATE_VERSION
        );
    }

    private static RoadmapItemType inferItemType(AiRoadmapStandardItem.Category category, String title) {
        String value = title == null ? "" : title;
        return switch (category) {
            case COURSE -> RoadmapItemType.OTHER;
            case PROJECT -> value.contains("미니") ? RoadmapItemType.MINI_PROJECT
                    : value.contains("팀") ? RoadmapItemType.TEAM_PROJECT : RoadmapItemType.PORTFOLIO_PROJECT;
            case CERTIFICATE -> RoadmapItemType.CERTIFICATE;
            case CONTEST -> value.contains("해커톤") ? RoadmapItemType.HACKATHON : RoadmapItemType.CONTEST;
            case INTERNSHIP -> RoadmapItemType.INTERNSHIP;
            case ETC -> RoadmapItemType.JOB_PREPARATION;
        };
    }

    private static String keywords(String... values) {
        String joined = Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
        return limit(joined, KEYWORD_MAX_LENGTH);
    }

    private static String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static DomainProfile profileOf(JobCategory category) {
        return switch (category) {
            case CONTENT_MEDIA -> new DomainProfile(
                    "콘텐츠 기획, 스토리텔링, 타깃·시장 분석, 저작권과 미디어 윤리",
                    "문서 작성, 이미지·영상 편집, 콘텐츠 관리와 채널 분석 도구",
                    "기획 의도와 타깃에 맞는 콘텐츠 설계·제작·배포·성과 분석",
                    "타깃과 채널을 정의한 콘텐츠 시리즈 또는 캠페인",
                    "콘텐츠 기획·디지털 마케팅·편집 도구 관련 교육과 인증",
                    "콘텐츠 제작, 미디어·출판·문화기관 운영 또는 마케팅 실무",
                    "기획서, 제작물, 배포 결과, 성과 지표와 개선 기록",
                    "기획 의도, 타깃 분석, 저작권 판단과 성과 개선 과정"
            );
            case LANGUAGE_EDUCATION -> new DomainProfile(
                    "언어 능력, 교육학·교수법, 교육과정 설계, 평가와 문화 간 의사소통",
                    "학습관리시스템, 문서·프레젠테이션, 번역·언어 자료 제작 도구",
                    "학습자·사용자 요구 분석, 교육·번역 콘텐츠 설계, 운영과 품질 평가",
                    "특정 학습자나 사용자를 위한 교육·번역·현지화 콘텐츠",
                    "어학 능력, 교원·교육, 번역·현지화 관련 자격과 교육 이수",
                    "교육기관, 에듀테크, 번역·현지화, 국제교류 운영 실무",
                    "수업안, 교재·콘텐츠, 평가 결과, 사용자 피드백과 개선 기록",
                    "대상자 분석, 교수·번역 전략, 품질 관리와 의사소통 사례"
            );
            case CULTURE_ART -> new DomainProfile(
                    "문화예술사, 전시·공연 기획, 관객 연구, 문화정책과 저작권",
                    "전시·행사 기획, 디지털 아카이브, 디자인·홍보와 운영 관리 도구",
                    "문화예술 자료 조사, 프로그램 기획, 예산·일정 운영과 관객 평가",
                    "전시·공연·교육·문화유산 활용 프로그램",
                    "학예·문화예술교육·문화행정 관련 자격과 실무 교육",
                    "박물관·미술관·문화재단·공연장·문화기관 프로젝트 실무",
                    "기획서, 큐레이션 자료, 운영 결과, 관객 반응과 개선 기록",
                    "기획 의도, 자료 선정 근거, 예산·일정 관리와 관객 경험 개선"
            );
            case LIBRARY_INFORMATION -> new DomainProfile(
                    "자료 조직, 분류·목록, 메타데이터, 정보검색, 기록관리와 저작권",
                    "도서관·아카이브 시스템, 데이터베이스, 메타데이터와 디지털화 도구",
                    "정보자원 수집·정리·검색·보존·서비스 설계와 이용자 지원",
                    "디지털 아카이브, 메타데이터 구축 또는 지식정보 서비스",
                    "사서·기록관리·정보관리와 데이터베이스 관련 자격과 교육",
                    "도서관, 기록관, 연구정보·학술DB, 기업 지식관리 실무",
                    "메타데이터 설계, 검색 구조, 구축 결과, 이용자 테스트와 운영 기록",
                    "정보 조직 원칙, 검색 품질, 개인정보·저작권과 이용자 서비스 판단"
            );
            case PUBLIC_POLICY_LAW -> new DomainProfile(
                    "행정학, 법·정책 분석, 조사방법론, 논리적 글쓰기와 공공 윤리",
                    "법령·판례 검색, 통계 분석, 정책 문서 작성과 협업 도구",
                    "문제 정의, 법·정책 근거 조사, 이해관계자 분석과 대안 평가",
                    "사회문제 해결을 위한 정책·법제·공공서비스 분석 보고서",
                    "행정·법무·정책분석·조사 관련 자격과 교육",
                    "공공기관, 연구기관, 법무·정책 프로젝트와 행정 실무",
                    "정책 보고서, 근거 데이터, 대안 비교, 이해관계자 분석과 제안",
                    "법·정책 근거, 공공성, 이해관계 조정과 대안 선택 과정"
            );
            case BUSINESS_TRADE -> new DomainProfile(
                    "경영, 경제, 마케팅, 운영관리, 국제무역과 비즈니스 커뮤니케이션",
                    "스프레드시트, ERP·CRM, 비즈니스 분석과 무역 문서 도구",
                    "시장·고객 분석, 사업 기획, 운영 개선, 성과 관리와 협상",
                    "시장 진입, 사업계획, 고객·브랜드 또는 업무 프로세스 개선 프로젝트",
                    "무역·유통·마케팅·ERP·비즈니스 분석 관련 자격과 교육",
                    "기업 기획·영업·마케팅·무역·운영 부서 또는 창업 프로젝트 실무",
                    "시장 자료, 가설, 실행 계획, 성과 지표와 의사결정 근거",
                    "시장·고객 판단, 수익성과 실행 가능성, 협업·협상 경험"
            );
            case FINANCE_ACCOUNTING -> new DomainProfile(
                    "회계, 재무, 경제, 통계, 위험관리와 금융 규제",
                    "스프레드시트, 회계·ERP, 금융 데이터와 분석 도구",
                    "재무제표 분석, 가치평가, 예산·원가 관리와 위험 판단",
                    "기업·산업 분석, 가치평가, 투자·위험 또는 회계 개선 프로젝트",
                    "회계·재무·투자·세무·데이터 분석 관련 자격과 교육",
                    "회계·재무·금융기관, 리서치, 감사·세무 또는 기업 분석 실무",
                    "분석 모델, 가정과 근거, 재무 지표, 위험 요인과 검증 기록",
                    "수치 근거, 회계·규제 판단, 위험 관리와 분석 가정 설명"
            );
            case REAL_ESTATE_URBAN -> new DomainProfile(
                    "부동산, 도시·교통계획, 입지·시장 분석, 법규와 공간 데이터",
                    "GIS, CAD, 통계·스프레드시트와 공간 분석 도구",
                    "입지·수요 조사, 개발·운영 타당성, 도시·교통 대안 분석",
                    "지역·입지 분석, 개발기획, 교통·도시 문제 해결 프로젝트",
                    "부동산·도시·교통·GIS·공간정보 관련 자격과 교육",
                    "개발·자산관리·도시계획·교통·공공기관 조사와 프로젝트 실무",
                    "공간 데이터, 현장 조사, 타당성 분석, 대안 비교와 시각화 결과",
                    "법규·입지 근거, 이해관계자 조정, 타당성과 공공성 판단"
            );
            case FASHION_BEAUTY -> new DomainProfile(
                    "소재·제품 이해, 디자인·상품기획, 소비자 분석, 브랜딩과 생산·유통",
                    "디자인·패턴·이미지 도구, 이커머스·상품관리와 채널 분석 도구",
                    "트렌드·고객 분석, 제품·서비스 기획, 제작·운영과 브랜드 관리",
                    "컬렉션, 제품·서비스, 브랜드 또는 판매 채널 기획 프로젝트",
                    "패션·뷰티·디자인·상품기획·마케팅 관련 자격과 교육",
                    "브랜드, 유통·이커머스, 제작, 매장·서비스 운영과 캠페인 실무",
                    "무드보드, 기획서, 시제품·콘텐츠, 고객 반응과 판매·운영 지표",
                    "트렌드 근거, 고객 정의, 제품·서비스 차별점과 실행 과정"
            );
            case DESIGN_MEDIA -> new DomainProfile(
                    "시각·인터랙션·영상 디자인 원리, 사용자 조사, 스토리텔링과 접근성",
                    "그래픽·프로토타이핑·영상·3D 제작과 협업 도구",
                    "문제 정의, 사용자·콘텐츠 분석, 시안 제작, 테스트와 반복 개선",
                    "사용자 문제를 해결하는 브랜드·UX/UI·영상·공간 디자인 결과물",
                    "디자인·영상·그래픽·UX 도구 관련 자격과 교육",
                    "디자인 스튜디오, 콘텐츠 제작, 제품팀, 전시·브랜드 프로젝트 실무",
                    "리서치, 콘셉트, 제작 과정, 프로토타입, 테스트 결과와 개선 기록",
                    "디자인 근거, 사용자 요구, 선택한 표현 방식과 피드백 반영 과정"
            );
            case SOFTWARE -> new DomainProfile(
                    "프로그래밍 언어, 자료구조·알고리즘, 데이터베이스, 운영체제와 네트워크",
                    "Git, SQL, 테스트, API 문서화, 배포와 협업 도구",
                    "요구사항 분석부터 설계·구현·테스트·배포까지의 소프트웨어 개발 과정",
                    "사용자 문제를 해결하는 웹·모바일·서버 또는 시스템 서비스",
                    "정보처리, SQL, 클라우드·개발 도구 관련 자격과 교육",
                    "개발 인턴, 산학 프로젝트, 오픈소스 또는 팀 소프트웨어 개발 실무",
                    "소스 코드, 설계 문서, 테스트 결과, 배포 링크와 문제 해결 기록",
                    "기술 선택, 설계 근거, 오류 해결, 테스트와 협업 경험"
            );
            case DATA_AI -> new DomainProfile(
                    "Python·SQL, 통계, 데이터 모델링, 머신러닝과 데이터·AI 윤리",
                    "노트북, 데이터베이스, 시각화, 버전관리, 모델·파이프라인 도구",
                    "데이터 수집·정제·분석·모델링·평가·배포와 결과 해석",
                    "실제 데이터를 활용한 분석, 예측 모델, 데이터 파이프라인 또는 AI 서비스",
                    "데이터 분석, SQL, 클라우드·AI 도구 관련 자격과 교육",
                    "데이터 분석·엔지니어링·AI 인턴, 연구·산학 또는 데이터 프로젝트 실무",
                    "데이터 정의, 전처리, 실험 기준, 모델·분석 결과, 한계와 재현 기록",
                    "데이터 품질, 지표 선택, 모델 평가, 해석 가능성과 윤리적 판단"
            );
            case ELECTRONICS_SEMICONDUCTOR -> new DomainProfile(
                    "회로, 디지털 논리, 신호·시스템, 반도체 소자·공정과 임베디드 기초",
                    "회로·EDA 시뮬레이션, 계측, PCB·임베디드 개발과 문서화 도구",
                    "요구 사양 분석, 회로·소자·시스템 설계, 시뮬레이션과 측정 검증",
                    "회로·임베디드·반도체 설계 또는 계측·공정 개선 프로젝트",
                    "전자·반도체·임베디드·품질 관련 자격과 교육",
                    "전자·반도체 기업, 연구실, 산학 프로젝트와 설계·공정·시험 실무",
                    "회로도·설계 파일, 실험 조건, 측정 결과, 오차 분석과 개선 기록",
                    "설계 사양, 측정·검증 방법, 오류 원인과 안전·품질 판단"
            );
            case MECHANICAL_ROBOTICS -> new DomainProfile(
                    "역학, 기계설계, 제어, 로봇·자동화, 재료와 제조 공정",
                    "CAD·CAE, 제어·로봇 시뮬레이션, 계측과 제작 도구",
                    "요구 사양 분석, 기구·제어 설계, 제작·시뮬레이션과 성능 검증",
                    "기계 장치, 로봇, 자동화 시스템 또는 제품 설계·제작 프로젝트",
                    "기계설계·제어·로봇·생산 관련 자격과 교육",
                    "설계·생산·자동화·로봇 기업, 연구실과 산학 프로젝트 실무",
                    "도면·모델, 설계 계산, 제작 과정, 시험 결과와 개선 기록",
                    "설계 근거, 안전·신뢰성, 성능 검증과 제작 문제 해결 과정"
            );
            case INDUSTRIAL_ENGINEERING -> new DomainProfile(
                    "운영연구, 품질관리, 생산·물류, 공급망, 인간공학과 데이터 분석",
                    "스프레드시트, 통계·최적화, 시뮬레이션, ERP·MES와 분석 도구",
                    "프로세스 측정, 원인 분석, 최적화·품질 개선과 성과 관리",
                    "생산·물류·서비스 프로세스 분석, 최적화 또는 스마트제조 프로젝트",
                    "품질·생산·물류·데이터·프로젝트 관리 관련 자격과 교육",
                    "생산·품질·물류·컨설팅·스마트제조 기업과 개선 프로젝트 실무",
                    "프로세스 맵, 데이터, 분석 모델, 개선안, 성과 지표와 검증 기록",
                    "문제 정의, 최적화 가정, 품질·비용·납기 균형과 변화관리"
            );
            case SECURITY -> new DomainProfile(
                    "네트워크, 운영체제, 암호, 보안 정책, 안전한 개발과 위협 분석",
                    "Linux, 패킷·로그 분석, 취약점 진단, SIEM과 코드 보안 도구",
                    "자산·위협 식별, 취약점 분석, 탐지·대응, 보안 설계와 보고",
                    "취약점 분석, 보안 모니터링, 사고 대응 또는 안전한 서비스 구축 프로젝트",
                    "정보보안·네트워크·클라우드·보안 도구 관련 자격과 교육",
                    "보안관제, 침해대응, 취약점 진단, 보안개발·정책 프로젝트 실무",
                    "분석 환경, 재현 절차, 로그·증적, 위험 평가와 개선 조치 기록",
                    "위협 모델, 위험도 판단, 대응 절차, 윤리·법규와 재발 방지"
            );
        };
    }

    public record StandardItemSeed(
            String seedKey,
            AiRoadmapStandardItem.Category category,
            Integer targetGrade,
            AiRoadmapStandardItem.Priority priority,
            Integer displayOrder,
            String title,
            String description,
            String keyword,
            String recommendationReason,
            String externalUrl,
            boolean requiredItem,
            RoadmapLane roadmapLane,
            RoadmapItemType itemType,
            RoadmapStage targetStage,
            boolean coreItem,
            boolean defaultIncluded,
            int templateVersion
    ) {
    }

    private static String firstClause(String value) {
        if (value == null || value.isBlank()) return "직무 핵심 주제";
        return value.split("[,·]")[0].trim();
    }

    private record DomainProfile(
            String foundation,
            String tools,
            String practice,
            String projectTheme,
            String credentialGuide,
            String fieldExperience,
            String portfolioEvidence,
            String interviewFocus
    ) {
    }
}
