package com.Hstep.Hstep.domain.airoadmap.dto;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapItem;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class AiRoadmapDto {

    public record ProfileRegistrationRequest(
            boolean certificateNone,
            boolean awardNone,
            boolean volunteerNone,
            boolean extraActivityNone
    ) {}

    public record EligibilityResponse(
            boolean available,
            String reasonCode,
            String message,
            boolean moveToMyPage
    ) {}

    public record JobRecommendationResponse(
            Long jobId,
            String jobName,
            int score,
            int trackScore,
            int specScore,
            int courseScore,
            int gradeScore,
            List<String> reasons
    ) {}

    public record CreateRoadmapRequest(@NotNull Long jobId) {}

    public record RoadmapResponse(
            Long aiRoadmapId,
            String userId,
            String userName,
            Integer currentGrade,
            BigDecimal gpa,
            Long interestJobId,
            String interestJobName,
            List<ItemResponse> items
    ) {}

    public record ItemResponse(
            Long aiRoadmapItemId,
            Long standardItemId,
            String title,
            AiRoadmapStandardItem.Category category,
            Integer targetGrade,
            AiRoadmapStandardItem.Priority priority,
            AiRoadmapItem.Status status,
            Integer displayOrder,
            String recommendationReason,
            String description,
            String externalUrl,
            boolean aiApplied
    ) {
        public static ItemResponse from(AiRoadmapItem item) {
            AiRoadmapStandardItem standard = item.getStandardItem();
            return new ItemResponse(
                    item.getAiRoadmapItemId(), standard.getStandardItemId(), standard.getTitle(),
                    standard.getCategory(), standard.getTargetGrade(), item.getPriority(), item.getStatus(),
                    standard.getDisplayOrder(), standard.getRecommendationReason(), standard.getDescription(),
                    standard.getExternalUrl(), item.isAiApplied()
            );
        }
    }

    public record ChatRequest(
            @NotBlank String message,
            @Min(2) @Max(5) Integer selectedGrade,
            AiRoadmapStandardItem.Category selectedCategory,
            Long targetRoadmapItemId,
            Long targetJobId
    ) {}

    public record ChatResponse(
            String message,
            AiRoadmapChangeProposal.ActionType actionType,
            boolean requiresConfirmation,
            ProposalResponse proposal,
            List<JobRecommendationResponse> jobOptions,
            ItemResponse referencedItem
    ) {}

    public record ProposalResponse(
            String proposalId,
            AiRoadmapChangeProposal.ActionType actionType,
            Long targetRoadmapItemId,
            Long targetStandardItemId,
            Long targetJobId,
            AiRoadmapStandardItem.Priority targetPriority,
            String message
    ) {
        public static ProposalResponse from(AiRoadmapChangeProposal proposal) {
            return new ProposalResponse(
                    proposal.getProposalId(), proposal.getActionType(), proposal.getTargetRoadmapItemId(),
                    proposal.getTargetStandardItemId(), proposal.getTargetJobId(), proposal.getTargetPriority(),
                    proposal.getMessage()
            );
        }
    }

    public record StandardItemRequest(
            @NotNull Long jobId,
            @NotNull AiRoadmapStandardItem.Category category,
            @NotNull @Min(2) @Max(5) Integer targetGrade,
            @NotNull AiRoadmapStandardItem.Priority priority,
            @NotNull @Min(1) Integer displayOrder,
            @NotBlank String title,
            String description,
            String keyword,
            String recommendationReason,
            String externalUrl,
            boolean requiredItem
    ) {}

    public record StandardItemResponse(
            Long standardItemId,
            Long jobId,
            String jobName,
            AiRoadmapStandardItem.Category category,
            Integer targetGrade,
            AiRoadmapStandardItem.Priority priority,
            Integer displayOrder,
            String title,
            String description,
            String keyword,
            String recommendationReason,
            String externalUrl,
            boolean requiredItem
    ) {
        public static StandardItemResponse from(AiRoadmapStandardItem item) {
            return new StandardItemResponse(
                    item.getStandardItemId(), item.getJob().getJobId(), item.getJob().getJobName(),
                    item.getCategory(), item.getTargetGrade(), item.getPriority(), item.getDisplayOrder(),
                    item.getTitle(), item.getDescription(), item.getKeyword(), item.getRecommendationReason(),
                    item.getExternalUrl(), item.isRequiredItem()
            );
        }
    }
}
