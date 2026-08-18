package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ai_roadmap_change_proposal", indexes = @Index(name = "idx_ai_proposal_user_status", columnList = "user_id,status"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRoadmapChangeProposal extends BaseEntity {

    public enum ActionType {
        CHANGE_INTEREST_JOB,
        ADD_ROADMAP_ITEM,
        ADD_CUSTOM_ITEM,
        EDIT_ITEM,
        MOVE_ITEM,
        REPLACE_ITEM,
        REMOVE_ITEM,
        COMPLETE_ITEM,
        REOPEN_ITEM,
        HIDE_ROADMAP_ITEM,
        COMPLETE_ROADMAP_ITEM,
        CHANGE_PRIORITY,
        EXPLAIN_ROADMAP_ITEM,
        NO_ACTION
    }

    public enum Status { PENDING, APPLIED, CANCELED }

    @Id
    @Column(name = "proposal_id", length = 36)
    private String proposalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_roadmap_id", nullable = false)
    private AiRoadmap aiRoadmap;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 40)
    private ActionType actionType;

    @Column(name = "target_roadmap_item_id")
    private Long targetRoadmapItemId;

    @Column(name = "target_standard_item_id")
    private Long targetStandardItemId;

    @Column(name = "target_job_id")
    private Long targetJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_priority", length = 20)
    private AiRoadmapStandardItem.Priority targetPriority;

    @Column(name = "message", length = 1000)
    private String message;

    @Lob
    @Column(name = "before_snapshot", columnDefinition = "LONGTEXT")
    private String beforeSnapshot;

    @Lob
    @Column(name = "after_snapshot", columnDefinition = "LONGTEXT")
    private String afterSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    private AiRoadmapChangeProposal(Member member, AiRoadmap aiRoadmap, ActionType actionType,
                                    Long targetRoadmapItemId, Long targetStandardItemId, Long targetJobId,
                                    AiRoadmapStandardItem.Priority targetPriority, String message) {
        this.proposalId = UUID.randomUUID().toString();
        this.member = member;
        this.aiRoadmap = aiRoadmap;
        this.actionType = actionType;
        this.targetRoadmapItemId = targetRoadmapItemId;
        this.targetStandardItemId = targetStandardItemId;
        this.targetJobId = targetJobId;
        this.targetPriority = targetPriority;
        this.message = message;
        this.status = Status.PENDING;
    }

    public static AiRoadmapChangeProposal create(Member member, AiRoadmap aiRoadmap, ActionType actionType,
                                                  Long targetRoadmapItemId, Long targetStandardItemId,
                                                  Long targetJobId, AiRoadmapStandardItem.Priority targetPriority,
                                                  String message) {
        return new AiRoadmapChangeProposal(member, aiRoadmap, actionType, targetRoadmapItemId,
                targetStandardItemId, targetJobId, targetPriority, message);
    }

    public static AiRoadmapChangeProposal create(Member member, AiRoadmap aiRoadmap, ActionType actionType,
                                                  Long targetRoadmapItemId, Long targetStandardItemId,
                                                  Long targetJobId, AiRoadmapStandardItem.Priority targetPriority,
                                                  String message, Map<String, Object> before,
                                                  Map<String, Object> after) {
        AiRoadmapChangeProposal proposal = create(member, aiRoadmap, actionType, targetRoadmapItemId,
                targetStandardItemId, targetJobId, targetPriority, message);
        proposal.beforeSnapshot = writeSnapshot(before);
        proposal.afterSnapshot = writeSnapshot(after);
        return proposal;
    }

    public Map<String, Object> readBeforeSnapshot() {
        return readSnapshot(beforeSnapshot);
    }

    public Map<String, Object> readAfterSnapshot() {
        return readSnapshot(afterSnapshot);
    }

    private static String writeSnapshot(Map<String, Object> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) return null;
        try {
            String json = JsonMapper.builder().build().writeValueAsString(snapshot);
            if (json.length() > 20_000) throw new IllegalArgumentException("변경 제안 payload가 너무 큽니다.");
            return json;
        } catch (Exception exception) {
            throw new IllegalArgumentException("변경 제안 payload를 직렬화할 수 없습니다.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readSnapshot(String snapshot) {
        if (snapshot == null || snapshot.isBlank()) return Map.of();
        try {
            return new LinkedHashMap<>(JsonMapper.builder().build().readValue(snapshot, Map.class));
        } catch (Exception exception) {
            throw new IllegalStateException("저장된 변경 제안 payload를 읽을 수 없습니다.", exception);
        }
    }

    public void apply() { this.status = Status.APPLIED; }
    public void cancel() { this.status = Status.CANCELED; }
}
