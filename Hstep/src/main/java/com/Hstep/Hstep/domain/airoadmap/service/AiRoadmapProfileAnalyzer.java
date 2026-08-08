package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapProfileRegistration;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapProfileRegistrationRepository;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.exception.MemberResponseCode;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.entity.Award;
import com.Hstep.Hstep.domain.profile.entity.Certificate;
import com.Hstep.Hstep.domain.profile.entity.ExtraActivity;
import com.Hstep.Hstep.domain.profile.entity.Volunteer;
import com.Hstep.Hstep.domain.profile.repository.AwardRepository;
import com.Hstep.Hstep.domain.profile.repository.CertificateRepository;
import com.Hstep.Hstep.domain.profile.repository.ExtraActivityRepository;
import com.Hstep.Hstep.domain.profile.repository.VolunteerRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AiRoadmapProfileAnalyzer {

    private final MemberRepository memberRepository;
    private final CertificateRepository certificateRepository;
    private final AwardRepository awardRepository;
    private final VolunteerRepository volunteerRepository;
    private final ExtraActivityRepository extraActivityRepository;
    private final AiRoadmapProfileRegistrationRepository registrationRepository;

    public Member getMemberWithTracks(String userId) {
        return memberRepository.findWithTracksByUserId(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));
    }

    public AiRoadmapDto.EligibilityResponse checkEligibility(String userId) {
        Member member = getMemberWithTracks(userId);
        if (member.getGrade() == null || member.getGrade() < 2) {
            return new AiRoadmapDto.EligibilityResponse(false, "GRADE_RESTRICTED",
                    "개인 맞춤 AI 로드맵은 2학년부터 이용할 수 있습니다.", false);
        }
        if (member.getMemberTracks().isEmpty()) {
            return new AiRoadmapDto.EligibilityResponse(false, "TRACK_REQUIRED",
                    "소속 트랙 정보가 필요합니다.", true);
        }
        if (member.getGpa() == null) {
            return new AiRoadmapDto.EligibilityResponse(false, "PROFILE_INCOMPLETE",
                    "개인 맞춤 AI 로드맵을 이용하려면 마이페이지에서 학점과 개인 스펙 정보를 먼저 등록해주세요.", true);
        }

        AiRoadmapProfileRegistration registration = registrationRepository.findByMember_UserId(userId).orElse(null);
        boolean certificateDone = !certificateRepository.findByMember_UserId(userId).isEmpty()
                || registration != null && registration.isCertificateNone();
        boolean awardDone = !awardRepository.findByMember_UserId(userId).isEmpty()
                || registration != null && registration.isAwardNone();
        boolean volunteerDone = !volunteerRepository.findByMember_UserId(userId).isEmpty()
                || registration != null && registration.isVolunteerNone();
        boolean extraDone = !extraActivityRepository.findByMember_UserId(userId).isEmpty()
                || registration != null && registration.isExtraActivityNone();

        if (!(certificateDone && awardDone && volunteerDone && extraDone)) {
            return new AiRoadmapDto.EligibilityResponse(false, "PROFILE_INCOMPLETE",
                    "개인 맞춤 AI 로드맵을 이용하려면 마이페이지에서 각 개인 스펙 항목을 등록하거나 해당 없음을 선택해주세요.", true);
        }
        return new AiRoadmapDto.EligibilityResponse(true, null, "AI 로드맵을 이용할 수 있습니다.", false);
    }

    @Transactional
    public void updateRegistration(String userId, AiRoadmapDto.ProfileRegistrationRequest request) {
        Member member = getMemberWithTracks(userId);
        AiRoadmapProfileRegistration registration = registrationRepository.findByMember_UserId(userId)
                .orElseGet(() -> registrationRepository.save(AiRoadmapProfileRegistration.create(member)));
        registration.update(request.certificateNone(), request.awardNone(), request.volunteerNone(), request.extraActivityNone());
    }

    public String buildProfileCorpus(String userId) {
        List<String> values = new ArrayList<>();
        for (Certificate certificate : certificateRepository.findByMember_UserId(userId)) {
            values.add(certificate.getCertificateName());
        }
        for (Award award : awardRepository.findByMember_UserId(userId)) {
            values.add(award.getCompetitionName());
            values.add(award.getAwardName());
            values.add(award.getDescription());
        }
        for (Volunteer volunteer : volunteerRepository.findByMember_UserId(userId)) {
            values.add(volunteer.getVolunteerName());
            values.add(volunteer.getDescription());
        }
        for (ExtraActivity activity : extraActivityRepository.findByMember_UserId(userId)) {
            values.add(activity.getActivityName());
            values.add(activity.getFieldKeyword());
            values.add(activity.getDescription());
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .reduce("", (left, right) -> left + " " + right);
    }
}
