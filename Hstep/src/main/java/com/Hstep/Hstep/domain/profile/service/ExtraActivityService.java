package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.ExtraActivityDto;
import com.Hstep.Hstep.domain.profile.entity.ExtraActivity;
import com.Hstep.Hstep.domain.profile.exception.ProfileResponseCode;
import com.Hstep.Hstep.domain.profile.repository.ExtraActivityRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExtraActivityService {

    private final ExtraActivityRepository extraActivityRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(String userId, ExtraActivityDto.Request request) {
        Member member = memberRepository.getReferenceById(userId);
        ExtraActivity activity = new ExtraActivity(
                request.activityName(), request.fieldKeyword(), request.period(), request.description(), member);
        return extraActivityRepository.save(activity).getActivityId();
    }

    public List<ExtraActivityDto.Response> findAllByUser(String userId) {
        return extraActivityRepository.findByMember_UserId(userId).stream()
                .map(ExtraActivityDto.Response::from)
                .toList();
    }

    @Transactional
    public void update(Long activityId, ExtraActivityDto.Request request) {
        ExtraActivity activity = extraActivityRepository.findById(activityId)
                .orElseThrow(() -> new BaseException(ProfileResponseCode.EXTRA_ACTIVITY_NOT_FOUND));
        activity.update(request.activityName(), request.fieldKeyword(), request.period(), request.description());
    }

    @Transactional
    public void delete(Long activityId) {
        if (!extraActivityRepository.existsById(activityId)) {
            throw new BaseException(ProfileResponseCode.EXTRA_ACTIVITY_NOT_FOUND);
        }
        extraActivityRepository.deleteById(activityId);
    }
}