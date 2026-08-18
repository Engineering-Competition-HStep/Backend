package com.Hstep.Hstep.domain.airoadmap.initializer;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class AiRoadmapStandardSeedCatalog {

    public static final int ITEMS_PER_JOB = 12;

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
        DomainProfile profile = profileOf(category);

        return List.of(
                seed(
                        "G2_FOUNDATION",
                        AiRoadmapStandardItem.Category.COURSE,
                        2,
                        AiRoadmapStandardItem.Priority.HIGH,
                        10,
                        normalizedJobName + " 기초 역량 학습",
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
                        normalizedJobName + " 직무·산업 리서치",
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
                        normalizedJobName + " 기초 미니 프로젝트",
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
                        normalizedJobName + " 핵심 실무 역량 강화",
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
                        normalizedJobName + " 포트폴리오 프로젝트",
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
                        normalizedJobName + " 심화 프로젝트 확장",
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
                        normalizedJobName + " 관련 자격증 준비",
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
                        normalizedJobName + " 인턴·현장실습 준비",
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
                        normalizedJobName + " 공모전·대외활동 참여",
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
                        normalizedJobName + " 포트폴리오 완성도 개선",
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
                        normalizedJobName + " 지원서·채용 공고 분석",
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
                        normalizedJobName + " 면접·실무 과제 대비",
                        profile.interviewFocus()
                                + "을 중심으로 예상 질문, 경험 기반 답변, 실무 과제와 발표를 반복 연습합니다.",
                        keywords(normalizedJobName, "면접", "실무 과제", "발표", "경험 기반 답변", profile.interviewFocus()),
                        "직무 지식과 프로젝트 경험을 제한된 시간 안에 논리적으로 설명하기 위한 필수 항목입니다.",
                        true
                )
        );
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
                requiredItem
        );
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
            boolean requiredItem
    ) {
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
