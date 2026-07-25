package com.Hstep.Hstep.domain.roadmap.repository;

import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BaseRoadmapRepository extends JpaRepository<BaseRoadmap, Long> {

    Optional<BaseRoadmap> findByTrack_TrackId(Long trackId);

    boolean existsByTrack_TrackId(Long trackId);

    List<BaseRoadmap> findByTrack_TrackIdIn(List<Long> trackIds);
}