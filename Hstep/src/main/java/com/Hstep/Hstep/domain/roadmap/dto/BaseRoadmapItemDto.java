package com.Hstep.Hstep.domain.roadmap.dto;

import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmapItem;
import com.Hstep.Hstep.domain.roadmap.entity.RoadmapLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BaseRoadmapItemDto {

    public record Request(
            @NotNull Integer itemOrder,
            @NotNull Integer grade,
            @NotNull Integer semester,
            @NotBlank String category,
            @NotNull RoadmapLevel level,
            @NotBlank String title,
            String description
    ) {}

    public record Response(
            Long itemId,
            Integer itemOrder,
            Integer grade,
            Integer semester,
            String category,
            RoadmapLevel level,
            String levelLabel,
            String title,
            String description
    ) {
        public static Response from(BaseRoadmapItem item) {
            return new Response(
                    item.getItemId(),
                    item.getItemOrder(),
                    item.getGrade(),
                    item.getSemester(),
                    item.getCategory(),
                    item.getLevel(),
                    item.getLevel().getLabel(),
                    item.getTitle(),
                    item.getDescription()
            );
        }
    }
}