package com.Hstep.Hstep.domain.roadmap.dto;

import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmap;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BaseRoadmapDto {

    public record Request(
            @NotNull Long trackId,
            @NotBlank String title
    ) {}

    public record UpdateRequest(
            @NotBlank String title
    ) {}

    public record Response(
            Long roadmapId,
            Long trackId,
            String trackName,
            String title,
            List<BaseRoadmapItemDto.Response> items
    ) {
        public static Response from(BaseRoadmap baseRoadmap) {
            return new Response(
                    baseRoadmap.getRoadmapId(),
                    baseRoadmap.getTrack().getTrackId(),
                    baseRoadmap.getTrack().getTrackName(),
                    baseRoadmap.getTitle(),
                    baseRoadmap.getItems().stream()
                            .map(BaseRoadmapItemDto.Response::from)
                            .toList()
            );
        }
    }
}