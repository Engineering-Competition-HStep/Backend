package com.Hstep.Hstep.domain.roadmap.dto;

import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmapItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BaseRoadmapItemDto {

    public record Request(
            @NotNull Integer itemOrder,
            @NotBlank String title,
            Integer grade,
            String description
    ) {}

    public record Response(
            Long itemId,
            Integer itemOrder,
            String title,
            Integer grade,
            String description
    ) {
        public static Response from(BaseRoadmapItem item) {
            return new Response(
                    item.getItemId(),
                    item.getItemOrder(),
                    item.getTitle(),
                    item.getGrade(),
                    item.getDescription()
            );
        }
    }
}