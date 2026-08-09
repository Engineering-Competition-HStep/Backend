package com.Hstep.Hstep.domain.airoadmap.controller;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.exception.AiRoadmapResponseCode;
import com.Hstep.Hstep.domain.airoadmap.service.AiRoadmapService;
import com.Hstep.Hstep.global.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-roadmaps/admin/standard-items")
public class AiRoadmapAdminController {

    private final AiRoadmapService aiRoadmapService;

    @GetMapping
    public SuccessResponse<List<AiRoadmapDto.StandardItemResponse>> getStandardItems(
            @RequestParam Long jobId
    ) {
        return SuccessResponse.of(
                aiRoadmapService.getStandardItems(jobId),
                AiRoadmapResponseCode.STANDARD_ITEM_MANAGE_SUCCESS
        );
    }

    @PostMapping
    public SuccessResponse<AiRoadmapDto.StandardItemResponse> createStandardItem(
            @Valid @RequestBody AiRoadmapDto.StandardItemRequest request
    ) {
        return SuccessResponse.of(
                aiRoadmapService.createStandardItem(request),
                AiRoadmapResponseCode.STANDARD_ITEM_MANAGE_SUCCESS
        );
    }

    @PutMapping("/{standardItemId}")
    public SuccessResponse<AiRoadmapDto.StandardItemResponse> updateStandardItem(
            @PathVariable Long standardItemId,
            @Valid @RequestBody AiRoadmapDto.StandardItemRequest request
    ) {
        return SuccessResponse.of(
                aiRoadmapService.updateStandardItem(standardItemId, request),
                AiRoadmapResponseCode.STANDARD_ITEM_MANAGE_SUCCESS
        );
    }

    @DeleteMapping("/{standardItemId}")
    public SuccessResponse<Void> deleteStandardItem(@PathVariable Long standardItemId) {
        aiRoadmapService.deleteStandardItem(standardItemId);
        return SuccessResponse.empty(AiRoadmapResponseCode.STANDARD_ITEM_MANAGE_SUCCESS);
    }
}
