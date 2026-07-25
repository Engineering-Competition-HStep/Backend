package com.Hstep.Hstep.domain.roadmap.repository;

import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmapItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseRoadmapItemRepository extends JpaRepository<BaseRoadmapItem, Long> {
}