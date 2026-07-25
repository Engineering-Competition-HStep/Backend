package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.UserGradeGpaDto;
import com.Hstep.Hstep.domain.profile.entity.UserGradeGpa;
import com.Hstep.Hstep.domain.profile.repository.UserGradeGpaRepository;
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

    // 학년 하나 저장 (있으면 수정, 없으면 생성)
    @Transactional
    public Long save(String userId, UserGradeGpaDto.Request request) {
        Long id = upsert(userId, request);
        recalculateOverallGpa(userId);
        return id;
    }

    // 여러 학년 내용 한 번에 저장 (마이페이지 '저장하기' 버튼)
    @Transactional
    public void saveAll(String userId, List<UserGradeGpaDto.Request> requests) {
        requests.forEach(request -> upsert(userId, request));
        recalculateOverallGpa(userId);
    }

    private Long upsert(String userId, UserGradeGpaDto.Request request) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. userId=" + userId));

        if (request.grade() > member.getGrade()) {
            throw new IllegalArgumentException("현재 학년보다 높은 학년의 학점은 입력할 수 없습니다.");
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
    public void delete(Long userGradeGpaId) {
        UserGradeGpa entity = userGradeGpaRepository.findById(userGradeGpaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학년별 평균 학점입니다. id=" + userGradeGpaId));
        String userId = entity.getMember().getUserId();
        userGradeGpaRepository.deleteById(userGradeGpaId);
        recalculateOverallGpa(userId);
    }

    private void recalculateOverallGpa(String userId) {
        List<UserGradeGpa> gradeGpas = userGradeGpaRepository.findByMember_UserId(userId);
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. userId=" + userId));

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