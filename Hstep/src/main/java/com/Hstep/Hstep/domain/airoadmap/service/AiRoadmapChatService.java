package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmap;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapItem;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.exception.AiRoadmapResponseCode;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapChangeProposalRepository;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiRoadmapChatService {

    private final AiRoadmapService aiRoadmapService;
    private final AiRoadmapProfileAnalyzer profileAnalyzer;
    private final AiRoadmapIntentClassifier intentClassifier;
    private final AiRoadmapChangeProposalRepository proposalRepository;

    public AiRoadmapDto.ChatResponse chat(String userId, AiRoadmapDto.ChatRequest request) {
        aiRoadmapService.requireEligible(userId);
        AiRoadmap roadmap = aiRoadmapService.findRoadmap(userId);
        AiRoadmapChangeProposal.ActionType actionType = intentClassifier.classify(request.message());

        return switch (actionType) {
            case CHANGE_INTEREST_JOB -> proposeJobChange(userId, roadmap, request);
            case ADD_ROADMAP_ITEM -> proposeItemAddition(userId, roadmap, request);
            case COMPLETE_ROADMAP_ITEM -> proposeItemChange(userId, roadmap, request,
                    AiRoadmapChangeProposal.ActionType.COMPLETE_ROADMAP_ITEM);
            case HIDE_ROADMAP_ITEM -> proposeItemChange(userId, roadmap, request,
                    AiRoadmapChangeProposal.ActionType.HIDE_ROADMAP_ITEM);
            case CHANGE_PRIORITY -> proposePriorityChange(userId, roadmap, request);
            case EXPLAIN_ROADMAP_ITEM -> explainItem(roadmap, request);
            case NO_ACTION -> guidePriorityOrSupportedRequest(roadmap, request);
        };
    }

    private AiRoadmapDto.ChatResponse proposeJobChange(String userId, AiRoadmap roadmap,
                                                         AiRoadmapDto.ChatRequest request) {
        List<AiRoadmapDto.JobRecommendationResponse> options = aiRoadmapService.recommendJobs(userId).stream()
                .filter(job -> !Objects.equals(job.jobId(), roadmap.getInterestJob().getJobId()))
                .toList();

        if (request.targetJobId() == null) {
            return new AiRoadmapDto.ChatResponse(
                    "변경 가능한 추천 직무를 선택해주세요. 직무를 선택해도 확인 전에는 로드맵이 변경되지 않습니다.",
                    AiRoadmapChangeProposal.ActionType.CHANGE_INTEREST_JOB,
                    false, null, options, null
            );
        }

        AiRoadmapDto.JobRecommendationResponse selected = options.stream()
                .filter(job -> Objects.equals(job.jobId(), request.targetJobId()))
                .findFirst()
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION));

        if (!aiRoadmapService.standardRepository().existsByJob_JobId(selected.jobId())) {
            throw new BaseException(AiRoadmapResponseCode.STANDARD_ROADMAP_NOT_FOUND);
        }

        String message = "관심 직무를 '" + roadmap.getInterestJob().getJobName() + "'에서 '"
                + selected.jobName() + "'(으)로 변경하고 표준 로드맵을 다시 구성합니다. "
                + "두 직무에 공통으로 존재하는 완료 항목은 완료 상태를 유지합니다.";
        AiRoadmapChangeProposal proposal = saveProposal(userId, roadmap,
                AiRoadmapChangeProposal.ActionType.CHANGE_INTEREST_JOB,
                null, null, selected.jobId(), null, message);

        return responseWithProposal(message, proposal, options, null);
    }

    private AiRoadmapDto.ChatResponse proposeItemAddition(String userId, AiRoadmap roadmap,
                                                            AiRoadmapDto.ChatRequest request) {
        AiRoadmapStandardItem.Category category = resolveRequestedCategory(request.message(), request.selectedCategory());
        if (category == null || (category != AiRoadmapStandardItem.Category.PROJECT
                && category != AiRoadmapStandardItem.Category.CERTIFICATE
                && request.selectedCategory() == null)) {
            return unsupportedGuide();
        }

        Member member = profileAnalyzer.getMemberWithTracks(userId);
        int grade = request.selectedGrade() != null ? request.selectedGrade() : member.getGrade();
        Set<Long> existingStandardIds = aiRoadmapService.getAllItems(roadmap).stream()
                .map(item -> item.getStandardItem().getStandardItemId())
                .collect(Collectors.toSet());

        List<AiRoadmapStandardItem> standards = aiRoadmapService.standardRepository()
                .findAllByJob_JobIdOrderByTargetGradeAscDisplayOrderAsc(roadmap.getInterestJob().getJobId());
        Optional<AiRoadmapStandardItem> candidate = standards.stream()
                .filter(item -> item.getCategory() == category)
                .filter(item -> !existingStandardIds.contains(item.getStandardItemId()))
                .filter(item -> Objects.equals(item.getTargetGrade(), grade))
                .sorted(Comparator.comparing(AiRoadmapStandardItem::getPriority)
                        .thenComparing(AiRoadmapStandardItem::getDisplayOrder))
                .findFirst();

        if (candidate.isEmpty()) {
            candidate = standards.stream()
                    .filter(item -> item.getCategory() == category)
                    .filter(item -> !existingStandardIds.contains(item.getStandardItemId()))
                    .sorted(Comparator.comparing(AiRoadmapStandardItem::getTargetGrade)
                            .thenComparing(AiRoadmapStandardItem::getPriority)
                            .thenComparing(AiRoadmapStandardItem::getDisplayOrder))
                    .findFirst();
        }

        if (candidate.isEmpty()) {
            return new AiRoadmapDto.ChatResponse(
                    "현재 기준 데이터에서 추가할 수 있는 " + categoryName(category) + " 항목이 없습니다. 기존 로드맵을 유지합니다.",
                    AiRoadmapChangeProposal.ActionType.NO_ACTION, false, null, List.of(), null
            );
        }

        AiRoadmapStandardItem standard = candidate.get();
        String message = "'" + standard.getTitle() + "' 활동을 " + gradeLabel(standard.getTargetGrade())
                + " / " + categoryName(standard.getCategory()) + "에 추가하는 것을 추천합니다."
                + reasonSuffix(standard.getRecommendationReason());
        AiRoadmapChangeProposal proposal = saveProposal(userId, roadmap,
                AiRoadmapChangeProposal.ActionType.ADD_ROADMAP_ITEM,
                null, standard.getStandardItemId(), null, standard.getPriority(), message);

        return responseWithProposal(message, proposal, List.of(), null);
    }

    private AiRoadmapDto.ChatResponse proposeItemChange(String userId, AiRoadmap roadmap,
                                                         AiRoadmapDto.ChatRequest request,
                                                         AiRoadmapChangeProposal.ActionType actionType) {
        AiRoadmapItem item = resolveTargetItem(roadmap, request);
        String verb = actionType == AiRoadmapChangeProposal.ActionType.COMPLETE_ROADMAP_ITEM
                ? "완료 상태로 변경" : "로드맵에서 제외";
        String message = "'" + item.getStandardItem().getTitle() + "' 항목을 " + verb
                + "합니다. 확인 후에만 실제 로드맵에 반영됩니다.";
        AiRoadmapChangeProposal proposal = saveProposal(userId, roadmap, actionType,
                item.getAiRoadmapItemId(), null, null, null, message);
        return responseWithProposal(message, proposal, List.of(), AiRoadmapDto.ItemResponse.from(item));
    }

    private AiRoadmapDto.ChatResponse proposePriorityChange(String userId, AiRoadmap roadmap,
                                                             AiRoadmapDto.ChatRequest request) {
        AiRoadmapItem item = resolveTargetItem(roadmap, request);
        AiRoadmapStandardItem.Priority priority = resolvePriority(request.message());
        String message = "'" + item.getStandardItem().getTitle() + "' 항목의 우선순위를 "
                + priorityName(priority) + "(으)로 변경합니다.";
        AiRoadmapChangeProposal proposal = saveProposal(userId, roadmap,
                AiRoadmapChangeProposal.ActionType.CHANGE_PRIORITY,
                item.getAiRoadmapItemId(), null, null, priority, message);
        return responseWithProposal(message, proposal, List.of(), AiRoadmapDto.ItemResponse.from(item));
    }

    private AiRoadmapDto.ChatResponse explainItem(AiRoadmap roadmap, AiRoadmapDto.ChatRequest request) {
        AiRoadmapItem item = resolveTargetItem(roadmap, request);
        AiRoadmapStandardItem standard = item.getStandardItem();
        String message = "'" + standard.getTitle() + "'은(는) "
                + Optional.ofNullable(standard.getDescription()).filter(value -> !value.isBlank())
                .orElse("등록된 상세 설명이 없는 활동입니다.")
                + reasonSuffix(standard.getRecommendationReason());
        return new AiRoadmapDto.ChatResponse(message,
                AiRoadmapChangeProposal.ActionType.EXPLAIN_ROADMAP_ITEM,
                false, null, List.of(), AiRoadmapDto.ItemResponse.from(item));
    }

    private AiRoadmapDto.ChatResponse guidePriorityOrSupportedRequest(AiRoadmap roadmap,
                                                                       AiRoadmapDto.ChatRequest request) {
        String normalized = normalize(request.message());
        if (containsAny(normalized, "가장먼저", "먼저해야", "우선활동", "뭐부터", "지금해야")) {
            int grade = request.selectedGrade() != null
                    ? request.selectedGrade()
                    : roadmap.getMember().getGrade();
            Optional<AiRoadmapItem> first = aiRoadmapService.getAllItems(roadmap).stream()
                    .filter(item -> item.getStatus() != AiRoadmapItem.Status.COMPLETED
                            && item.getStatus() != AiRoadmapItem.Status.HIDDEN)
                    .filter(item -> Objects.equals(item.getStandardItem().getTargetGrade(), grade))
                    .sorted(Comparator.comparing(AiRoadmapItem::getPriority)
                            .thenComparing(item -> item.getStandardItem().getDisplayOrder()))
                    .findFirst();
            if (first.isPresent()) {
                AiRoadmapItem item = first.get();
                String message = "현재 가장 먼저 준비할 활동은 '" + item.getStandardItem().getTitle()
                        + "'입니다." + reasonSuffix(item.getStandardItem().getRecommendationReason());
                return new AiRoadmapDto.ChatResponse(message, AiRoadmapChangeProposal.ActionType.NO_ACTION,
                        false, null, List.of(), AiRoadmapDto.ItemResponse.from(item));
            }
            return new AiRoadmapDto.ChatResponse("현재 학년에 남아 있는 미완료 로드맵 항목이 없습니다.",
                    AiRoadmapChangeProposal.ActionType.NO_ACTION, false, null, List.of(), null);
        }
        return unsupportedGuide();
    }

    private AiRoadmapDto.ChatResponse unsupportedGuide() {
        return new AiRoadmapDto.ChatResponse(
                "현재는 프로젝트·자격증 추천 및 추가, 우선 활동 안내, 활동 완료·제외, 활동 설명, 우선순위 변경, 관심 직무 변경을 지원합니다.",
                AiRoadmapChangeProposal.ActionType.NO_ACTION, false, null, List.of(), null
        );
    }

    @Transactional
    public AiRoadmapDto.RoadmapResponse apply(String userId, String proposalId) {
        AiRoadmapChangeProposal proposal = findPendingProposal(userId, proposalId);
        AiRoadmap roadmap = aiRoadmapService.findRoadmap(userId);

        switch (proposal.getActionType()) {
            case ADD_ROADMAP_ITEM -> applyAdd(roadmap, proposal);
            case HIDE_ROADMAP_ITEM -> aiRoadmapService.findOwnedItem(roadmap, proposal.getTargetRoadmapItemId()).hide();
            case COMPLETE_ROADMAP_ITEM -> aiRoadmapService.findOwnedItem(roadmap, proposal.getTargetRoadmapItemId()).complete(true);
            case CHANGE_PRIORITY -> aiRoadmapService.findOwnedItem(roadmap, proposal.getTargetRoadmapItemId())
                    .changePriority(requirePriority(proposal));
            case CHANGE_INTEREST_JOB -> applyJobChange(userId, roadmap, proposal);
            case EXPLAIN_ROADMAP_ITEM, NO_ACTION -> throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }
        proposal.apply();
        Member member = profileAnalyzer.getMemberWithTracks(userId);
        AiRoadmap refreshed = aiRoadmapService.findRoadmap(userId);
        return aiRoadmapService.toRoadmapResponse(member, refreshed, null, null);
    }

    private void applyAdd(AiRoadmap roadmap, AiRoadmapChangeProposal proposal) {
        AiRoadmapStandardItem standard = aiRoadmapService.findStandardItem(proposal.getTargetStandardItemId());
        if (!Objects.equals(standard.getJob().getJobId(), roadmap.getInterestJob().getJobId())) {
            throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }
        if (aiRoadmapService.itemRepository()
                .findByAiRoadmap_AiRoadmapIdAndStandardItem_StandardItemId(
                        roadmap.getAiRoadmapId(), standard.getStandardItemId()).isPresent()) {
            throw new BaseException(AiRoadmapResponseCode.INVALID_PROPOSAL_STATE);
        }
        aiRoadmapService.itemRepository().save(
                AiRoadmapItem.create(roadmap, standard, AiRoadmapItem.Status.PENDING, true)
        );
    }

    private void applyJobChange(String userId, AiRoadmap oldRoadmap, AiRoadmapChangeProposal proposal) {
        Long targetJobId = proposal.getTargetJobId();
        if (targetJobId == null || Objects.equals(targetJobId, oldRoadmap.getInterestJob().getJobId())) {
            throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }

        Set<String> completedKeys = aiRoadmapService.getAllItems(oldRoadmap).stream()
                .filter(item -> item.getStatus() == AiRoadmapItem.Status.COMPLETED)
                .map(item -> completionKey(item.getStandardItem()))
                .collect(Collectors.toSet());

        aiRoadmapService.replaceInterestJob(userId, targetJobId);
        AiRoadmap newRoadmap = aiRoadmapService.findRoadmap(userId);
        for (AiRoadmapItem item : aiRoadmapService.getAllItems(newRoadmap)) {
            if (completedKeys.contains(completionKey(item.getStandardItem()))) {
                item.complete(true);
            }
        }
    }

    @Transactional
    public void cancel(String userId, String proposalId) {
        AiRoadmapChangeProposal proposal = findPendingProposal(userId, proposalId);
        proposal.cancel();
    }

    private AiRoadmapChangeProposal findPendingProposal(String userId, String proposalId) {
        AiRoadmapChangeProposal proposal = proposalRepository.findByProposalIdAndMember_UserId(proposalId, userId)
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.PROPOSAL_NOT_FOUND));
        if (proposal.getStatus() != AiRoadmapChangeProposal.Status.PENDING) {
            throw new BaseException(AiRoadmapResponseCode.INVALID_PROPOSAL_STATE);
        }
        return proposal;
    }

    private AiRoadmapItem resolveTargetItem(AiRoadmap roadmap, AiRoadmapDto.ChatRequest request) {
        if (request.targetRoadmapItemId() != null) {
            return aiRoadmapService.findOwnedItem(roadmap, request.targetRoadmapItemId());
        }
        String message = normalize(request.message());
        return aiRoadmapService.getAllItems(roadmap).stream()
                .filter(item -> item.getStatus() != AiRoadmapItem.Status.HIDDEN)
                .filter(item -> {
                    String title = normalize(item.getStandardItem().getTitle());
                    return message.contains(title) || title.contains(message);
                })
                .findFirst()
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.AI_ROADMAP_ITEM_NOT_FOUND));
    }

    private AiRoadmapChangeProposal saveProposal(String userId, AiRoadmap roadmap,
                                                   AiRoadmapChangeProposal.ActionType actionType,
                                                   Long targetRoadmapItemId, Long targetStandardItemId,
                                                   Long targetJobId, AiRoadmapStandardItem.Priority targetPriority,
                                                   String message) {
        Member member = profileAnalyzer.getMemberWithTracks(userId);
        return proposalRepository.save(AiRoadmapChangeProposal.create(member, roadmap, actionType,
                targetRoadmapItemId, targetStandardItemId, targetJobId, targetPriority, message));
    }

    private AiRoadmapDto.ChatResponse responseWithProposal(String message, AiRoadmapChangeProposal proposal,
                                                            List<AiRoadmapDto.JobRecommendationResponse> jobOptions,
                                                            AiRoadmapDto.ItemResponse item) {
        return new AiRoadmapDto.ChatResponse(message, proposal.getActionType(), true,
                AiRoadmapDto.ProposalResponse.from(proposal), jobOptions, item);
    }

    private AiRoadmapStandardItem.Category resolveRequestedCategory(String message,
                                                                     AiRoadmapStandardItem.Category selected) {
        if (selected != null) return selected;
        String normalized = normalize(message);
        if (normalized.contains("프로젝트")) return AiRoadmapStandardItem.Category.PROJECT;
        if (normalized.contains("자격증")) return AiRoadmapStandardItem.Category.CERTIFICATE;
        return null;
    }

    private AiRoadmapStandardItem.Priority resolvePriority(String message) {
        String normalized = normalize(message);
        if (containsAny(normalized, "높", "최우선", "먼저")) return AiRoadmapStandardItem.Priority.HIGH;
        if (containsAny(normalized, "낮", "나중")) return AiRoadmapStandardItem.Priority.LOW;
        return AiRoadmapStandardItem.Priority.MEDIUM;
    }

    private AiRoadmapStandardItem.Priority requirePriority(AiRoadmapChangeProposal proposal) {
        if (proposal.getTargetPriority() == null) {
            throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }
        return proposal.getTargetPriority();
    }

    private String completionKey(AiRoadmapStandardItem standard) {
        return standard.getCategory().name() + "|" + normalize(standard.getTitle());
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private boolean containsAny(String value, String... keywords) {
        return Arrays.stream(keywords).anyMatch(value::contains);
    }

    private String reasonSuffix(String reason) {
        return reason == null || reason.isBlank() ? "" : " 추천 이유: " + reason;
    }

    private String categoryName(AiRoadmapStandardItem.Category category) {
        return switch (category) {
            case CONTEST -> "공모전";
            case PROJECT -> "프로젝트";
            case CERTIFICATE -> "자격증";
            case INTERNSHIP -> "인턴·대외활동";
            case COURSE -> "수업";
            case ETC -> "기타 활동";
        };
    }

    private String gradeLabel(Integer grade) {
        if (grade != null && grade == 5) return "취업 준비";
        return grade + "학년";
    }

    private String priorityName(AiRoadmapStandardItem.Priority priority) {
        return switch (priority) {
            case HIGH -> "높음";
            case MEDIUM -> "보통";
            case LOW -> "낮음";
        };
    }
}
