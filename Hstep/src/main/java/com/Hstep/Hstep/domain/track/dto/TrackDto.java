package com.Hstep.Hstep.domain.track.dto;

import com.Hstep.Hstep.domain.track.entity.Track;

public class TrackDto {

    public record Response(
            Long trackId,
            String trackCode,
            String trackName
    ) {
        public static Response from(Track track) {
            return new Response(track.getTrackId(), track.getTrackCode(), track.getTrackName());
        }
    }
}
