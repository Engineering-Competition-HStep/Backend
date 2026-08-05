package com.Hstep.Hstep.domain.profile.repository;

import com.Hstep.Hstep.domain.profile.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AwardRepository extends JpaRepository<Award, Long> {
    List<Award> findByMember_UserId(String userId);
    Optional<Award> findByAwardIdAndMember_UserId(Long awardId, String userId);
}