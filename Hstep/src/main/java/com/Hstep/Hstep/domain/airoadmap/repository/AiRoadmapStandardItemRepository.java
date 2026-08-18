package com.Hstep.Hstep.domain.airoadmap.repository;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AiRoadmapStandardItemRepository extends JpaRepository<AiRoadmapStandardItem, Long> {

    @EntityGraph(attributePaths = "job")
    List<AiRoadmapStandardItem> findAllByJob_JobIdOrderByTargetGradeAscDisplayOrderAsc(Long jobId);

    List<AiRoadmapStandardItem> findAllByJob_JobIdAndTargetGradeOrderByDisplayOrderAsc(Long jobId, Integer targetGrade);

    boolean existsByJob_JobId(Long jobId);

    /**
     * 서버 시작 시 모든 표준 항목을 한 번에 비교할 수 있도록 JOB까지 함께 조회합니다.
     * 직무별 exists 쿼리를 반복하지 않아 초기화 과정의 N+1 조회를 방지합니다.
     */
    @Query("""
            select item
            from AiRoadmapStandardItem item
            join fetch item.job
            order by item.job.jobName, item.targetGrade, item.displayOrder
            """)
    List<AiRoadmapStandardItem> findAllWithJob();
}
