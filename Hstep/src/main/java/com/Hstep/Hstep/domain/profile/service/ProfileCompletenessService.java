// domain/profile/service/ProfileCompletenessService.java
package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.exception.MemberResponseCode;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.ProfileCompletenessDto;
import com.Hstep.Hstep.domain.profile.repository.AwardRepository;
import com.Hstep.Hstep.domain.profile.repository.CertificateRepository;
import com.Hstep.Hstep.domain.profile.repository.ExtraActivityRepository;
import com.Hstep.Hstep.domain.profile.repository.UserGradeGpaRepository;
import com.Hstep.Hstep.domain.profile.repository.VolunteerRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileCompletenessService {

    private final MemberRepository memberRepository;
    private final UserGradeGpaRepository userGradeGpaRepository;
    private final CertificateRepository certificateRepository;
    private final AwardRepository awardRepository;
    private final VolunteerRepository volunteerRepository;
    private final ExtraActivityRepository extraActivityRepository;

    public ProfileCompletenessDto.Response check(String userId) {
        Member member = memberRepository.findWithTracksByUserId(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));

        boolean trackCompleted = !member.getMemberTracks().isEmpty();

        boolean gradeCompleted = !userGradeGpaRepository.findByMember_UserId(userId).isEmpty();

        boolean specCompleted =
                !certificateRepository.findByMember_UserId(userId).isEmpty()
                        || !awardRepository.findByMember_UserId(userId).isEmpty()
                        || !volunteerRepository.findByMember_UserId(userId).isEmpty()
                        || !extraActivityRepository.findByMember_UserId(userId).isEmpty();

        return ProfileCompletenessDto.Response.of(trackCompleted, gradeCompleted, specCompleted);
    }
}