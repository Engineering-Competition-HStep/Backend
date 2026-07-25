package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.VolunteerDto;
import com.Hstep.Hstep.domain.profile.entity.Volunteer;
import com.Hstep.Hstep.domain.profile.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(String userId, VolunteerDto.Request request) {
        Member member = memberRepository.getReferenceById(userId);
        Volunteer volunteer = new Volunteer(request.volunteerName(), request.volunteerHours(), request.description(), member);
        return volunteerRepository.save(volunteer).getVolunteerId();
    }

    public List<VolunteerDto.Response> findAllByUser(String userId) {
        return volunteerRepository.findByMember_UserId(userId).stream()
                .map(VolunteerDto.Response::from)
                .toList();
    }

    @Transactional
    public void update(Long volunteerId, VolunteerDto.Request request) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 봉사활동입니다. id=" + volunteerId));
        volunteer.update(request.volunteerName(), request.volunteerHours(), request.description());
    }

    @Transactional
    public void delete(Long volunteerId) {
        if (!volunteerRepository.existsById(volunteerId)) {
            throw new IllegalArgumentException("존재하지 않는 봉사활동입니다. id=" + volunteerId);
        }
        volunteerRepository.deleteById(volunteerId);
    }
}