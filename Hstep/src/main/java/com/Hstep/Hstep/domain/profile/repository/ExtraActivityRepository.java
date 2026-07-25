package com.Hstep.Hstep.domain.profile.repository;

import com.Hstep.Hstep.domain.profile.entity.ExtraActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExtraActivityRepository extends JpaRepository<ExtraActivity, Long> {
    List<ExtraActivity> findByMember_UserId(String userId);
}