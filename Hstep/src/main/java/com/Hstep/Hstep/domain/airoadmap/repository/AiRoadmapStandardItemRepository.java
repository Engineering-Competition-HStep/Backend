package com.Hstep.Hstep.domain.airoadmap.repository;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiRoadmapStandardItemRepository extends JpaRepository<AiRoadmapStandardItem, Long> {

    @EntityGraph(attributePaths = "job")
    List<AiRoadmapStandardItem> findAllByJob_JobIdOrderByTargetGradeAscDisplayOrderAsc(Long jobId);

    List<AiRoadmapStandardItem> findAllByJob_JobIdAndTargetGradeOrderByDisplayOrderAsc(Long jobId, Integer targetGrade);

    boolean existsByJob_JobId(Long jobId);
}
