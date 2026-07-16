package com.Hstep.Hstep.domain.member.service;

import com.Hstep.Hstep.domain.member.dto.MemberDto.MemberRes;
import com.Hstep.Hstep.domain.member.dto.MemberDto.UpdateReq;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.exception.MemberResponseCode;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberRes getMe(Long userId) {
        return MemberRes.fromEntity(getMemberWithTracks(userId));
    }

    @Transactional
    public MemberRes updateMe(Long userId, UpdateReq updateReq) {
        Member member = getMemberWithTracks(userId);

        member.updateProfile(updateReq.getName(), updateReq.getGrade(), updateReq.getGpa());

        if (updateReq.getTrackIds() != null) {
            validateTracks(updateReq.getTrackIds());
            member.replaceTracks(updateReq.getTrackIds());
        }

        return MemberRes.fromEntity(member);
    }

    @Transactional
    public void deleteMe(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));
        memberRepository.delete(member);
    }

    private Member getMemberWithTracks(Long userId) {
        return memberRepository.findWithTracksByUserId(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));
    }

    private void validateTracks(List<Long> trackIds) {
        long distinctTrackCount = trackIds.stream().distinct().count();
        if (trackIds.isEmpty() || trackIds.size() > 2 || distinctTrackCount != trackIds.size()) {
            throw new BaseException(MemberResponseCode.INVALID_TRACKS);
        }
    }
}
