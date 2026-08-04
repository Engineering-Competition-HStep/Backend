package com.Hstep.Hstep.domain.job.repository;

import com.Hstep.Hstep.domain.job.entity.JobTrack;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobTrackRepository extends JpaRepository<JobTrack, Long> {

    boolean existsByTrack_TrackIdAndJob_JobId(Long trackId, Long jobId);

    @EntityGraph(attributePaths = "job")
    List<JobTrack> findAllByTrack_TrackIdOrderByJob_JobNameAsc(Long trackId);

    @EntityGraph(attributePaths = "track")
    List<JobTrack> findAllByJob_JobIdOrderByTrack_TrackNameAsc(Long jobId);
}
