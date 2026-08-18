package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmap;
import com.Hstep.Hstep.domain.airoadmap.repository.*;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import com.Hstep.Hstep.domain.job.entity.JobTrack;
import com.Hstep.Hstep.domain.job.repository.JobRepository;
import com.Hstep.Hstep.domain.job.repository.JobTrackRepository;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiRoadmapEntryServiceTest {

    private final AiRoadmapRepository roadmapRepository = mock(AiRoadmapRepository.class);
    private final AiRoadmapItemRepository itemRepository = mock(AiRoadmapItemRepository.class);
    private final JobTrackRepository jobTrackRepository = mock(JobTrackRepository.class);
    private final AiRoadmapProfileAnalyzer analyzer = mock(AiRoadmapProfileAnalyzer.class);
    private final JobRecommendationProfileCatalog profiles = mock(JobRecommendationProfileCatalog.class);
    private final TrackRepository trackRepository = mock(TrackRepository.class);
    private AiRoadmapService service;

    @BeforeEach
    void setUp() {
        service = new AiRoadmapService(roadmapRepository, itemRepository,
                mock(AiRoadmapStandardItemRepository.class), mock(JobRepository.class),
                jobTrackRepository, analyzer, mock(RoadmapEvidenceMatcher.class), profiles, trackRepository);
    }

    @Test
    void 학년_제한은_404가_아닌_GRADE_RESTRICTED_상태로_응답한다() {
        Member member = member(1, false);
        when(analyzer.getMemberWithTracks("user")).thenReturn(member);
        when(analyzer.checkEligibility("user")).thenReturn(new AiRoadmapDto.EligibilityResponse(
                false, "GRADE_RESTRICTED", "2학년부터 이용", false));

        AiRoadmapDto.EntryResponse response = service.getEntry("user");

        assertThat(response.state()).isEqualTo(AiRoadmapDto.EntryState.GRADE_RESTRICTED);
        assertThat(response.roadmap()).isNull();
    }

    @Test
    void 프로필_미완료는_PROFILE_REQUIRED_상태로_응답한다() {
        Member member = member(3, true);
        when(analyzer.getMemberWithTracks("user")).thenReturn(member);
        when(analyzer.checkEligibility("user")).thenReturn(new AiRoadmapDto.EligibilityResponse(
                false, "PROFILE_INCOMPLETE", "정보 등록 필요", true));

        AiRoadmapDto.EntryResponse response = service.getEntry("user");

        assertThat(response.state()).isEqualTo(AiRoadmapDto.EntryState.PROFILE_REQUIRED);
        assertThat(response.moveToMyPage()).isTrue();
    }

    @Test
    void 프로필이_완료되고_로드맵이_없으면_추천과_JOB_SELECTION_REQUIRED를_반환한다() {
        Member member = member(3, true);
        Track track = new Track("WEB", "웹공학트랙");
        ReflectionTestUtils.setField(track, "trackId", 1L);
        Job job = Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트");
        ReflectionTestUtils.setField(job, "jobId", 10L);
        JobTrack relation = JobTrack.create(track, job);
        when(analyzer.getMemberWithTracks("user")).thenReturn(member);
        when(analyzer.checkEligibility("user")).thenReturn(new AiRoadmapDto.EligibilityResponse(
                true, null, "이용 가능", false));
        when(analyzer.buildProfileCorpus("user")).thenReturn("");
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));
        when(roadmapRepository.findByMember_UserId("user")).thenReturn(Optional.empty());
        when(jobTrackRepository.findAllByTrack_TrackIdOrderByJob_JobNameAsc(1L)).thenReturn(List.of(relation));
        when(jobTrackRepository.findAllByJob_JobIdOrderByTrack_TrackNameAsc(10L)).thenReturn(List.of(relation));
        when(profiles.resolve(job)).thenReturn(new JobRecommendationProfileCatalog.JobSkillProfile(
                Set.of("java"), Set.of("자료구조"), "BACKEND"));
        when(profiles.familyOf("백엔드개발자")).thenReturn("BACKEND");

        AiRoadmapDto.EntryResponse response = service.getEntry("user");

        assertThat(response.state()).isEqualTo(AiRoadmapDto.EntryState.JOB_SELECTION_REQUIRED);
        assertThat(response.recommendedJobs()).extracting(AiRoadmapDto.JobRecommendationResponse::jobId)
                .containsExactly(10L);
        assertThat(response.roadmap()).isNull();
    }

    @Test
    void 로드맵이_있으면_추천을_재계산하지_않고_ROADMAP_READY를_반환한다() {
        Member member = member(3, true);
        Job job = Job.create("백엔드개발자", JobCategory.SOFTWARE, "테스트");
        AiRoadmap roadmap = AiRoadmap.create(member, job);
        when(analyzer.getMemberWithTracks("user")).thenReturn(member);
        when(analyzer.checkEligibility("user")).thenReturn(new AiRoadmapDto.EligibilityResponse(
                true, null, "이용 가능", false));
        when(roadmapRepository.findByMember_UserId("user")).thenReturn(Optional.of(roadmap));
        when(itemRepository.findAllByAiRoadmap_AiRoadmapId(null)).thenReturn(List.of());

        AiRoadmapDto.EntryResponse response = service.getEntry("user");

        assertThat(response.state()).isEqualTo(AiRoadmapDto.EntryState.ROADMAP_READY);
        assertThat(response.recommendedJobs()).isEmpty();
        assertThat(response.roadmap()).isNotNull();
    }

    private Member member(int grade, boolean withTrack) {
        Member member = Member.create("user", "user@test.com", "encoded", "사용자", grade);
        member.updateProfile(null, null, new BigDecimal("3.50"));
        if (withTrack) member.addTracks(List.of(1L));
        return member;
    }
}
