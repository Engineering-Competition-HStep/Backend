package com.Hstep.Hstep.domain.airoadmap.repository;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapProfileRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiRoadmapProfileRegistrationRepository extends JpaRepository<AiRoadmapProfileRegistration, Long> {
    Optional<AiRoadmapProfileRegistration> findByMember_UserId(String userId);
}
