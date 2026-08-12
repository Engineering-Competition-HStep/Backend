package com.Hstep.Hstep.domain.track.service;

import com.Hstep.Hstep.domain.track.dto.TrackDto;
import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackService {

    private final TrackRepository trackRepository;

    public List<TrackDto.Response> findAll() {
        return trackRepository.findAll().stream()
                .sorted(Comparator.comparing(Track::getTrackId))
                .map(TrackDto.Response::from)
                .toList();
    }
}
