package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.AwardDto;
import com.Hstep.Hstep.domain.profile.entity.Award;
import com.Hstep.Hstep.domain.profile.repository.AwardRepository;
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
    public void update(Long awardId, AwardDto.Request request) {
        Award award = awardRepository.findById(awardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수상경력입니다. id=" + awardId));
        award.update(request.competitionName(), request.awardName(), request.awardRank(), request.description());
    }

    @Transactional
    public void delete(Long awardId) {
        if (!awardRepository.existsById(awardId)) {
            throw new IllegalArgumentException("존재하지 않는 수상경력입니다. id=" + awardId);
        }
        awardRepository.deleteById(awardId);
    }
}