package com.Hstep.Hstep.domain.airoadmap.repository;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmap;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiRoadmapRepository extends JpaRepository<AiRoadmap, Long> {

    @EntityGraph(attributePaths = "interestJob")
    Optional<AiRoadmap> findByMember_UserId(String userId);

    boolean existsByMember_UserId(String userId);
}
