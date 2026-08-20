package com.Hstep.Hstep.domain.track.initializer;


import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrackDataInitializer implements ApplicationRunner {

    private final TrackRepository trackRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<TrackSeed> seeds = createTrackSeeds();
        List<Track> newTracks = new ArrayList<>();

        for (TrackSeed seed : seeds) {
            if (!trackRepository.existsByTrackCode(seed.code())) {
                newTracks.add(new Track(seed.code(), seed.name()));
            }
        }

        if (newTracks.isEmpty()) {
            log.info("초기 트랙 데이터가 이미 모두 저장되어 있습니다.");
            return;
        }

        trackRepository.saveAll(newTracks);
        log.info("초기 트랙 데이터 {}개를 저장했습니다.", newTracks.size());
    }

    private List<TrackSeed> createTrackSeeds() {
        return List.of(
                // 1학년이 선택하는 단과대학
                seed("COLLEGE_CREATIVE_HUMANITIES_ARTS", "크리에이티브인문예술대학"),
                seed("COLLEGE_DESIGN", "디자인대학"),
                seed("COLLEGE_FUTURE_CONVERGENCE_SOCIAL_SCIENCE", "미래융합사회과학대학"),
                seed("COLLEGE_IT_ENGINEERING", "IT공과대학"),

                // 크리에이티브인문예술대학
                seed("TRACK_ENGLISH_AMERICAN_CULTURE_CONTENTS", "영미문화콘텐츠트랙"),
                seed("TRACK_ENGLISH_LANGUAGE_INFORMATION", "영미언어정보트랙"),
                seed("TRACK_KOREAN_LANGUAGE_EDUCATION", "한국어교육트랙"),
                seed("TRACK_HISTORY_CULTURE_CONTENTS", "역사문화콘텐츠트랙"),
                seed("TRACK_HISTORY_CONTENTS", "역사콘텐츠트랙"),
                seed("TRACK_KNOWLEDGE_INFORMATION_CULTURE", "지식정보문화트랙"),
                seed("TRACK_DIGITAL_HUMANITIES_INFORMATION", "디지털인문정보학트랙"),
                seed("MAJOR_ORIENTAL_PAINTING", "동양화전공"),
                seed("MAJOR_WESTERN_PAINTING", "서양화전공"),
                seed("MAJOR_KOREAN_DANCE", "한국무용전공"),
                seed("MAJOR_MODERN_DANCE", "현대무용전공"),
                seed("MAJOR_BALLET", "발레전공"),

                // 디자인대학
                seed("TRACK_FASHION_MARKETING", "패션마케팅트랙"),
                seed("TRACK_FASHION_DESIGN", "패션디자인트랙"),
                seed("TRACK_FASHION_CREATIVE_DIRECTION", "패션크리에이티브디렉션트랙"),
                seed("TRACK_VIDEO_ANIMATION_DESIGN", "영상·애니메이션디자인트랙"),
                seed("TRACK_UX_UI_DESIGN", "UX/UI디자인트랙"),
                seed("TRACK_GAME_GRAPHIC_DESIGN", "게임그래픽디자인트랙"),
                seed("TRACK_INTERIOR_DESIGN", "인테리어디자인트랙"),
                seed("TRACK_VMD_EXHIBITION_DESIGN", "VMD·전시디자인트랙"),
                seed("DEPARTMENT_BEAUTY_DESIGN_MANAGEMENT", "뷰티디자인매니지먼트학과"),

                // 미래융합사회과학대학
                seed("TRACK_INTERNATIONAL_TRADE", "국제무역트랙"),
                seed("TRACK_GLOBAL_BUSINESS", "글로벌비즈니스트랙"),
                seed("TRACK_CORPORATE_ECONOMIC_ANALYSIS", "기업·경제분석트랙"),
                seed("TRACK_ECONOMIC_FINANCE_INVESTMENT", "경제금융투자트랙"),
                seed("TRACK_PUBLIC_ADMINISTRATION", "공공행정트랙"),
                seed("TRACK_LAW_POLICY", "법&정책트랙"),
                seed("TRACK_REAL_ESTATE", "부동산트랙"),
                seed("TRACK_SMART_CITY_TRANSPORTATION_PLANNING", "스마트도시·교통계획트랙"),
                seed("TRACK_BUSINESS_MANAGEMENT", "기업경영트랙"),
                seed("TRACK_VENTURE_MANAGEMENT", "벤처경영트랙"),
                seed("TRACK_ACCOUNTING_FINANCIAL_MANAGEMENT", "회계·재무경영트랙"),

                // IT공과대학
                seed("TRACK_MOBILE_SOFTWARE", "모바일소프트웨어트랙"),
                seed("TRACK_BIG_DATA", "빅데이터트랙"),
                seed("TRACK_DIGITAL_CONTENTS_VR", "디지털콘텐츠·가상현실트랙"),
                seed("TRACK_WEB_ENGINEERING", "웹공학트랙"),
                seed("TRACK_ELECTRONICS", "전자트랙"),
                seed("TRACK_SYSTEM_SEMICONDUCTOR", "시스템반도체트랙"),
                seed("TRACK_MECHANICAL_DESIGN", "기계설계트랙"),
                seed("TRACK_MECHANICAL_AUTOMATION", "기계자동화트랙"),
                seed("TRACK_INDUSTRIAL_ENGINEERING", "산업공학트랙"),
                seed("TRACK_INTELLIGENT_SYSTEM", "지능시스템트랙"),

                // 신규 트랙 (BaseRoadmap 데이터 검토 과정에서 추가)
                seed("NEW-005", "이민ㆍ다문화트랙"),
                seed("NEW-008", "금융ㆍ데이터분석트랙"),
                seed("NEW-016", "미디어디자인트랙"),
                seed("NEW-017", "비즈니스애널리틱스트랙"),
                seed("NEW-022", "AI로봇융합트랙"),
                seed("NEW-023", "기계시스템디자인트랙"),
                seed("NEW-033", "스마트제조혁신컨설팅학과"),
                seed("NEW-034", "응용산업데이터공학트랙"),
                seed("NEW-036", "역사문화큐레이션트랙"),
                seed("NEW-037", "미래모빌리티학과"),
                seed("NEW-038", "시각디자인트랙"),
                seed("NEW-039", "문학문화콘텐츠학과"),
                seed("NEW-040", "융합보안학과"),
                seed("NEW-041", "AI응용학과"),
                seed("NEW-043", "정보시스템트랙")
        );
    }

    private TrackSeed seed(String code, String name) {
        return new TrackSeed(code, name);
    }

    private record TrackSeed(String code, String name) {
    }
}
