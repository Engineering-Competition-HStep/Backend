package com.Hstep.Hstep.domain.profile.repository;

import com.Hstep.Hstep.domain.profile.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    List<Volunteer> findByMember_UserId(String userId);
}