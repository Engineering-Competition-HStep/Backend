package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.exception.MemberResponseCode;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.UserGradeGpaDto;
import com.Hstep.Hstep.domain.profile.entity.UserGradeGpa;
import com.Hstep.Hstep.domain.profile.exception.ProfileResponseCode;
import com.Hstep.Hstep.domain.profile.repository.UserGradeGpaRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGradeGpaService {

    private final UserGradeGpaRepository userGradeGpaRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long save(String userId, UserGradeGpaDto.Request request) {
        Long id = upsert(userId, request);
        recalculateOverallGpa(userId);
        return id;
    }

    @Transactional
    public void saveAll(String userId, List<UserGradeGpaDto.Request> requests) {
        requests.forEach(request -> upsert(userId, request));
        recalculateOverallGpa(userId);
    }

    private Long upsert(String userId, UserGradeGpaDto.Request request) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));

        if (request.grade() > member.getGrade()) {
            throw new BaseException(ProfileResponseCode.INVALID_GRADE);
        }

        return userGradeGpaRepository.findByMember_UserIdAndGrade(userId, request.grade())
                .map(existing -> {
                    existing.update(request.gpa());
                    return existing.getUserGradeGpaId();
                })
                .orElseGet(() -> {
                    UserGradeGpa entity = userGradeGpaRepository.save(
                            new UserGradeGpa(request.grade(), request.gpa(), member));
                    return entity.getUserGradeGpaId();
                });
    }

    public List<UserGradeGpaDto.Response> findAllByUser(String userId) {
        return userGradeGpaRepository.findByMember_UserId(userId).stream()
                .map(UserGradeGpaDto.Response::from)
                .toList();
    }

    @Transactional
    public void delete(String userId, Long userGradeGpaId) {
        UserGradeGpa entity = userGradeGpaRepository.findByUserGradeGpaIdAndMember_UserId(userGradeGpaId, userId)
                .orElseThrow(() -> new BaseException(ProfileResponseCode.USER_GRADE_GPA_NOT_FOUND));
        userGradeGpaRepository.deleteById(userGradeGpaId);
        recalculateOverallGpa(userId);
    }

    private void recalculateOverallGpa(String userId) {
        List<UserGradeGpa> gradeGpas = userGradeGpaRepository.findByMember_UserId(userId);
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));

        if (gradeGpas.isEmpty()) {
            member.updateGpa(null);
            return;
        }

        BigDecimal sum = gradeGpas.stream()
                .map(UserGradeGpa::getGpa)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(gradeGpas.size()), 2, RoundingMode.HALF_UP);
        member.updateGpa(average);
    }
}