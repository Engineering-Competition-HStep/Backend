package com.Hstep.Hstep.domain.job.entity;

public enum JobCategory {
    CONTENT_MEDIA("콘텐츠·미디어"),
    LANGUAGE_EDUCATION("언어·교육"),
    CULTURE_ART("문화·예술"),
    LIBRARY_INFORMATION("문헌정보·아카이브"),
    PUBLIC_POLICY_LAW("공공·정책·법무"),
    BUSINESS_TRADE("경영·무역"),
    FINANCE_ACCOUNTING("금융·회계"),
    REAL_ESTATE_URBAN("부동산·도시·교통"),
    FASHION_BEAUTY("패션·뷰티"),
    DESIGN_MEDIA("디자인·영상"),
    SOFTWARE("소프트웨어"),
    DATA_AI("데이터·AI"),
    ELECTRONICS_SEMICONDUCTOR("전자·반도체"),
    MECHANICAL_ROBOTICS("기계·로봇"),
    INDUSTRIAL_ENGINEERING("산업공학·스마트제조"),
    SECURITY("정보보안");

    private final String displayName;

    JobCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
