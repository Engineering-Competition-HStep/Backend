package com.Hstep.Hstep.domain.airoadmap.dto;

import com.Hstep.Hstep.domain.airoadmap.entity.*;
import com.Hstep.Hstep.domain.chat.entity.ChatRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    public enum EntryState { GRADE_RESTRICTED, PROFILE_REQUIRED, JOB_SELECTION_REQUIRED, ROADMAP_READY }

    public record MemberTrackResponse(Long trackId, String trackName) {}

    public record EntryResponse(
            EntryState state,
            String reasonCode,
            String message,
            boolean moveToMyPage,
            List<MemberTrackResponse> memberTracks,
            List<JobRecommendationResponse> recommendedJobs,
            RoadmapResponse roadmap
    ) {}

    public record RoadmapResponse(
            Long aiRoadmapId,
            String userId,
            String userName,
            Integer currentGrade,
            BigDecimal gpa,
            Long interestJobId,
            String interestJobName,
            List<ItemResponse> items,
            RoadmapSummary summary
    ) {}

    public record RoadmapSummary(
            int totalCount,
            int completedCount,
            int needsImprovementCount,
            int pendingCount,
            int progressRate,
            Map<RoadmapLane, Long> laneCounts
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
            boolean aiApplied,
            LocalDateTime updatedAt,
            RoadmapLane roadmapLane,
            RoadmapItemType itemType,
            RoadmapStage targetStage,
            boolean coreItem,
            RoadmapItemSourceType sourceType
    ) {
        public static ItemResponse from(AiRoadmapItem item) {
            AiRoadmapStandardItem standard = item.getStandardItem();
            return new ItemResponse(
                    item.getAiRoadmapItemId(), standard == null ? null : standard.getStandardItemId(), item.getTitle(),
                    item.getCategory(), item.getTargetGrade(), item.getPriority(), item.getStatus(),
                    item.getDisplayOrder(), item.getRecommendationReason(), item.getDescription(),
                    item.getExternalUrl(), item.isAiApplied(), item.getUpdatedAt(), item.getRoadmapLane(),
                    item.getItemType(), item.getTargetStage(), item.isCoreItem(), item.getSourceType()
            );
        }
    }

    public record ChatRequest(
            @NotBlank String message,
            @Min(2) @Max(5) Integer selectedGrade,
            AiRoadmapStandardItem.Category selectedCategory,
            Long targetRoadmapItemId,
            Long targetJobId,
            RoadmapStage selectedStage,
            RoadmapLane selectedLane,
            RoadmapItemType selectedItemType,
            RoadmapItemDraft after
    ) {}

    public record RoadmapItemDraft(
            String title,
            String description,
            RoadmapLane roadmapLane,
            RoadmapItemType itemType,
            RoadmapStage targetStage,
            Integer displayOrder,
            AiRoadmapStandardItem.Priority priority
    ) {}

    public record ChatResponse(
            String message,
            AiRoadmapChangeProposal.ActionType actionType,
            boolean requiresConfirmation,
            ProposalResponse proposal,
            List<JobRecommendationResponse> jobOptions,
            ItemResponse referencedItem,
            RoadmapDiffResponse roadmapDiff
    ) {}

    public record RoadmapDiffResponse(
            List<String> maintainedItems,
            List<String> addedItems,
            List<String> removedItems
    ) {}

    public record ProposalResponse(
            String proposalId,
            AiRoadmapChangeProposal.ActionType actionType,
            Long targetRoadmapItemId,
            Long targetStandardItemId,
            Long targetJobId,
            AiRoadmapStandardItem.Priority targetPriority,
            String message,
            Map<String, Object> before,
            Map<String, Object> after,
            AiRoadmapChangeProposal.Status status
    ) {
        public static ProposalResponse from(AiRoadmapChangeProposal proposal) {
            return new ProposalResponse(
                    proposal.getProposalId(), proposal.getActionType(), proposal.getTargetRoadmapItemId(),
                    proposal.getTargetStandardItemId(), proposal.getTargetJobId(), proposal.getTargetPriority(),
                    proposal.getMessage(), proposal.readBeforeSnapshot(), proposal.readAfterSnapshot(), proposal.getStatus()
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
            boolean requiredItem,
            RoadmapLane roadmapLane,
            RoadmapItemType itemType,
            RoadmapStage targetStage,
            Boolean coreItem,
            Boolean defaultIncluded
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
            boolean requiredItem,
            RoadmapLane roadmapLane,
            RoadmapItemType itemType,
            RoadmapStage targetStage,
            boolean coreItem,
            boolean defaultIncluded,
            Integer templateVersion
    ) {
        public static StandardItemResponse from(AiRoadmapStandardItem item) {
            return new StandardItemResponse(
                    item.getStandardItemId(), item.getJob().getJobId(), item.getJob().getJobName(),
                    item.getCategory(), item.getTargetGrade(), item.getPriority(), item.getDisplayOrder(),
                    item.getTitle(), item.getDescription(), item.getKeyword(), item.getRecommendationReason(),
                    item.getExternalUrl(), item.isRequiredItem(), item.getRoadmapLaneEffective(),
                    item.getItemTypeEffective(), item.getTargetStageEffective(), item.isCoreItemEffective(),
                    item.isDefaultIncludedEffective(), item.getTemplateVersion()
            );
        }
    }

    public record ChatHistoryResponse(Long chatRoomId, List<ChatHistoryMessageResponse> messages) {}

    public record ChatHistoryMessageResponse(
            Long messageId,
            ChatRole role,
            String content,
            String proposalId,
            AiRoadmapChangeProposal.Status proposalStatus,
            boolean actionable,
            LocalDateTime createdAt
    ) {}
}
