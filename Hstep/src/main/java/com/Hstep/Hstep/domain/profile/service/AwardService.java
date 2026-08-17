package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.AwardDto;
import com.Hstep.Hstep.domain.profile.entity.Award;
import com.Hstep.Hstep.domain.profile.exception.ProfileResponseCode;
import com.Hstep.Hstep.domain.profile.repository.AwardRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AwardService {

    private final AwardRepository awardRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(String userId, AwardDto.Request request) {
        Member member = memberRepository.getReferenceById(userId);
        Award award = new Award(request.competitionName(), request.awardName(), request.awardRank(), request.description(), member);
        return awardRepository.save(award).getAwardId();
    }

    public List<AwardDto.Response> findAllByUser(String userId) {
        return awardRepository.findByMember_UserId(userId).stream()
                .map(AwardDto.Response::from)
                .toList();
    }

    @Transactional
    public void update(String userId, Long awardId, AwardDto.Request request) {
        Award award = getOwnedAward(userId, awardId);
        award.update(request.competitionName(), request.awardName(), request.awardRank(), request.description());
    }

    @Transactional
    public void delete(String userId, Long awardId) {
        Award award = getOwnedAward(userId, awardId);
        awardRepository.delete(award);
    }

    private Award getOwnedAward(String userId, Long awardId) {
        return awardRepository.findByAwardIdAndMember_UserId(awardId, userId)
                .orElseThrow(() -> new BaseException(ProfileResponseCode.AWARD_NOT_FOUND));
    }
}