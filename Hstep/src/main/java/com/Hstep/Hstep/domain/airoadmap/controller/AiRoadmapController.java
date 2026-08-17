package com.Hstep.Hstep.domain.airoadmap.controller;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.exception.AiRoadmapResponseCode;
import com.Hstep.Hstep.domain.airoadmap.service.AiRoadmapChatService;
import com.Hstep.Hstep.domain.airoadmap.service.AiRoadmapService;
import com.Hstep.Hstep.global.response.SuccessResponse;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-roadmaps")
public class AiRoadmapController {

    private final AiRoadmapService aiRoadmapService;
    private final AiRoadmapChatService aiRoadmapChatService;

    @GetMapping("/eligibility")
    public SuccessResponse<AiRoadmapDto.EligibilityResponse> checkEligibility(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return SuccessResponse.of(
                aiRoadmapService.checkEligibility(principal.getUserId()),
                AiRoadmapResponseCode.ELIGIBILITY_GET_SUCCESS
        );
    }

    @PutMapping("/profile-registration")
    public SuccessResponse<Void> updateProfileRegistration(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody AiRoadmapDto.ProfileRegistrationRequest request
    ) {
        aiRoadmapService.updateProfileRegistration(principal.getUserId(), request);
        return SuccessResponse.empty(AiRoadmapResponseCode.PROFILE_REGISTRATION_SUCCESS);
    }

    @GetMapping("/jobs/recommendations")
    public SuccessResponse<List<AiRoadmapDto.JobRecommendationResponse>> recommendJobs(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return SuccessResponse.of(
                aiRoadmapService.recommendJobs(principal.getUserId()),
                AiRoadmapResponseCode.JOB_RECOMMENDATION_SUCCESS
        );
    }

    @PostMapping
    public SuccessResponse<AiRoadmapDto.RoadmapResponse> createRoadmap(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody AiRoadmapDto.CreateRoadmapRequest request
    ) {
        return SuccessResponse.of(
                aiRoadmapService.createInitialRoadmap(principal.getUserId(), request.jobId()),
                AiRoadmapResponseCode.ROADMAP_CREATE_SUCCESS
        );
    }

    @GetMapping("/me")
    public SuccessResponse<AiRoadmapDto.RoadmapResponse> getMyRoadmap(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) AiRoadmapStandardItem.Category category
    ) {
        return SuccessResponse.of(
                aiRoadmapService.getMyRoadmap(principal.getUserId(), grade, category),
                AiRoadmapResponseCode.ROADMAP_GET_SUCCESS
        );
    }

    @PatchMapping("/items/{roadmapItemId}/complete")
    public SuccessResponse<AiRoadmapDto.ItemResponse> completeItem(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long roadmapItemId
    ) {
        return SuccessResponse.of(
                aiRoadmapService.completeItem(principal.getUserId(), roadmapItemId),
                AiRoadmapResponseCode.ROADMAP_ITEM_UPDATE_SUCCESS
        );
    }

    @PatchMapping("/items/{roadmapItemId}/reopen")
    public SuccessResponse<AiRoadmapDto.ItemResponse> reopenItem(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long roadmapItemId
    ) {
        return SuccessResponse.of(
                aiRoadmapService.reopenItem(principal.getUserId(), roadmapItemId),
                AiRoadmapResponseCode.ROADMAP_ITEM_UPDATE_SUCCESS
        );
    }

    @PostMapping("/chat")
    public SuccessResponse<AiRoadmapDto.ChatResponse> chat(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody AiRoadmapDto.ChatRequest request
    ) {
        return SuccessResponse.of(
                aiRoadmapChatService.chat(principal.getUserId(), request),
                AiRoadmapResponseCode.CHAT_SUCCESS
        );
    }

    @PostMapping("/chat/proposals/{proposalId}/apply")
    public SuccessResponse<AiRoadmapDto.RoadmapResponse> applyProposal(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable String proposalId
    ) {
        return SuccessResponse.of(
                aiRoadmapChatService.apply(principal.getUserId(), proposalId),
                AiRoadmapResponseCode.PROPOSAL_APPLY_SUCCESS
        );
    }

    @DeleteMapping("/chat/proposals/{proposalId}")
    public SuccessResponse<Void> cancelProposal(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable String proposalId
    ) {
        aiRoadmapChatService.cancel(principal.getUserId(), proposalId);
        return SuccessResponse.empty(AiRoadmapResponseCode.PROPOSAL_CANCEL_SUCCESS);
    }
}
