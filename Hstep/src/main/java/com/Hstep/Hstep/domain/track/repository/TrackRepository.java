package com.Hstep.Hstep.domain.track.repository;


import com.Hstep.Hstep.domain.track.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    boolean existsByTrackCode(String trackCode);

    Optional<Track> findByTrackCode(String trackCode);
}
