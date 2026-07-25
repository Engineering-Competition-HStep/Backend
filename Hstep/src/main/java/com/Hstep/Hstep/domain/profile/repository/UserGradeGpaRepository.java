package com.Hstep.Hstep.domain.profile.repository;

import com.Hstep.Hstep.domain.profile.entity.UserGradeGpa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserGradeGpaRepository extends JpaRepository<UserGradeGpa, Long> {
    List<UserGradeGpa> findByMember_UserId(String userId);
    Optional<UserGradeGpa> findByMember_UserIdAndGrade(String userId, Integer grade);
}