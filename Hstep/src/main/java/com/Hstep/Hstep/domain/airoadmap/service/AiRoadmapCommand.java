package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;

public record AiRoadmapCommand(
        AiRoadmapChangeProposal.ActionType actionType,
        Long targetRoadmapItemId,
        Long targetJobId,
        AiRoadmapDto.RoadmapItemDraft after,
        String reason
) {
}
