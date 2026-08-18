package com.Hstep.Hstep.domain.airoadmap.repository;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiRoadmapItemRepository extends JpaRepository<AiRoadmapItem, Long> {

    @EntityGraph(attributePaths = "standardItem")
    List<AiRoadmapItem> findAllByAiRoadmap_AiRoadmapId(Long aiRoadmapId);

    @EntityGraph(attributePaths = "standardItem")
    Optional<AiRoadmapItem> findByAiRoadmap_AiRoadmapIdAndStandardItem_StandardItemId(Long aiRoadmapId, Long standardItemId);

}
