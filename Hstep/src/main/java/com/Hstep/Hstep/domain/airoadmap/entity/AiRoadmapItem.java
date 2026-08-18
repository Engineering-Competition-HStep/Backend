package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "ai_roadmap_item", uniqueConstraints = @UniqueConstraint(
        name = "uk_ai_roadmap_standard_item", columnNames = {"ai_roadmap_id", "standard_item_id"}
))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRoadmapItem extends BaseEntity {

    public enum Status { PENDING, NEEDS_IMPROVEMENT, COMPLETED, HIDDEN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_roadmap_item_id")
    private Long aiRoadmapItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_roadmap_id", nullable = false)
    private AiRoadmap aiRoadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_item_id")
    private AiRoadmapStandardItem standardItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30)
    private RoadmapItemSourceType sourceType;

    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "recommendation_reason", length = 500)
    private String recommendationReason;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "legacy_category", length = 30)
    private AiRoadmapStandardItem.Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "roadmap_lane", length = 30)
    private RoadmapLane roadmapLane;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 40)
    private RoadmapItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_stage", length = 30)
    private RoadmapStage targetStage;

    @Column(name = "target_grade")
    private Integer targetGrade;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "core_item")
    private Boolean coreItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private AiRoadmapStandardItem.Priority priority;

    @Column(name = "ai_applied", nullable = false)
    private boolean aiApplied;

    private AiRoadmapItem(AiRoadmap aiRoadmap, AiRoadmapStandardItem standardItem, Status status,
                          AiRoadmapStandardItem.Priority priority, boolean aiApplied) {
        this.aiRoadmap = aiRoadmap;
        this.standardItem = standardItem;
        this.status = status;
        this.priority = priority;
        this.aiApplied = aiApplied;
    }

    public static AiRoadmapItem create(AiRoadmap aiRoadmap, AiRoadmapStandardItem standardItem,
                                       Status status, boolean aiApplied) {
        return fromStandard(aiRoadmap, standardItem, status, aiApplied);
    }

    public static AiRoadmapItem fromStandard(AiRoadmap aiRoadmap, AiRoadmapStandardItem standardItem,
                                             Status status, boolean aiApplied) {
        AiRoadmapItem item = new AiRoadmapItem(aiRoadmap, standardItem, status,
                standardItem.getPriority(), aiApplied);
        item.copySnapshot(standardItem);
        return item;
    }

    public static AiRoadmapItem createCustom(AiRoadmap aiRoadmap, String title, String description,
                                              RoadmapLane lane, RoadmapItemType itemType,
                                              RoadmapStage stage, Integer displayOrder,
                                              AiRoadmapStandardItem.Priority priority) {
        AiRoadmapItem item = new AiRoadmapItem(aiRoadmap, null, Status.PENDING,
                priority != null ? priority : AiRoadmapStandardItem.Priority.MEDIUM, true);
        item.sourceType = RoadmapItemSourceType.AI_ADDED;
        item.title = requireTitle(title);
        item.description = validateLength(description, 1000, "description");
        item.roadmapLane = Objects.requireNonNull(lane, "roadmapLane은 필수입니다.");
        item.itemType = itemType != null ? itemType : RoadmapItemType.OTHER;
        item.targetStage = Objects.requireNonNull(stage, "targetStage는 필수입니다.");
        item.targetGrade = stage.getGrade();
        item.displayOrder = displayOrder != null ? displayOrder : 999;
        item.category = legacyCategory(lane, item.itemType);
        item.coreItem = false;
        return item;
    }

    public void complete(boolean aiApplied) {
        this.status = Status.COMPLETED;
        this.aiApplied = aiApplied;
    }

    // 완료 체크를 잘못 눌렀거나 취소하고 싶을 때 이전 상태로 롤백
    public void reopen() {
        this.status = Status.PENDING;
    }

    public void hide() {
        this.status = Status.HIDDEN;
        this.aiApplied = true;
    }

    public void changePriority(AiRoadmapStandardItem.Priority priority) {
        this.priority = Objects.requireNonNull(priority, "priority는 필수입니다.");
        this.aiApplied = true;
    }

    public void edit(String title, String description) {
        if (title != null) this.title = requireTitle(title);
        if (description != null) this.description = validateLength(description, 1000, "description");
        this.aiApplied = true;
    }

    public void move(RoadmapStage stage, RoadmapLane lane, Integer displayOrder) {
        if (stage != null) {
            this.targetStage = stage;
            this.targetGrade = stage.getGrade();
        }
        if (lane != null) {
            this.roadmapLane = lane;
            this.category = legacyCategory(lane, getItemType());
        }
        if (displayOrder != null) this.displayOrder = displayOrder;
        this.aiApplied = true;
    }

    public void changeItemType(RoadmapItemType itemType) {
        this.itemType = Objects.requireNonNull(itemType, "itemType은 필수입니다.");
        this.category = legacyCategory(getRoadmapLane(), itemType);
        this.aiApplied = true;
    }

    public void backfillSnapshotIfMissing() {
        if (title == null && standardItem != null) copySnapshot(standardItem);
    }

    public String getTitle() {
        return title != null ? title : standardItem == null ? null : standardItem.getTitle();
    }

    public String getDescription() {
        return description != null ? description : standardItem == null ? null : standardItem.getDescription();
    }

    public String getRecommendationReason() {
        return recommendationReason != null ? recommendationReason
                : standardItem == null ? null : standardItem.getRecommendationReason();
    }

    public String getExternalUrl() {
        return externalUrl != null ? externalUrl : standardItem == null ? null : standardItem.getExternalUrl();
    }

    public AiRoadmapStandardItem.Category getCategory() {
        return category != null ? category : standardItem == null ? null : standardItem.getCategory();
    }

    public RoadmapLane getRoadmapLane() {
        return roadmapLane != null ? roadmapLane
                : standardItem == null ? RoadmapLane.EXPERIENCE : standardItem.getRoadmapLaneEffective();
    }

    public RoadmapItemType getItemType() {
        return itemType != null ? itemType
                : standardItem == null ? RoadmapItemType.OTHER : standardItem.getItemTypeEffective();
    }

    public RoadmapStage getTargetStage() {
        return targetStage != null ? targetStage
                : standardItem == null ? null : standardItem.getTargetStageEffective();
    }

    public Integer getTargetGrade() {
        return targetGrade != null ? targetGrade
                : standardItem == null ? null : standardItem.getTargetGrade();
    }

    public Integer getDisplayOrder() {
        return displayOrder != null ? displayOrder
                : standardItem == null ? 999 : standardItem.getDisplayOrder();
    }

    public boolean isCoreItem() {
        return coreItem != null ? coreItem
                : standardItem != null && standardItem.isCoreItemEffective();
    }

    public RoadmapItemSourceType getSourceType() {
        return sourceType != null ? sourceType : RoadmapItemSourceType.STANDARD_TEMPLATE;
    }

    private void copySnapshot(AiRoadmapStandardItem standard) {
        this.sourceType = RoadmapItemSourceType.STANDARD_TEMPLATE;
        this.title = standard.getTitle();
        this.description = standard.getDescription();
        this.recommendationReason = standard.getRecommendationReason();
        this.externalUrl = standard.getExternalUrl();
        this.category = standard.getCategory();
        this.roadmapLane = standard.getRoadmapLaneEffective();
        this.itemType = standard.getItemTypeEffective();
        this.targetStage = standard.getTargetStageEffective();
        this.targetGrade = standard.getTargetGrade();
        this.displayOrder = standard.getDisplayOrder();
        this.coreItem = standard.isCoreItemEffective();
    }

    private static String requireTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title은 필수입니다.");
        return validateLength(title.trim(), 120, "title");
    }

    private static String validateLength(String value, int max, String field) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException(field + "의 최대 길이는 " + max + "자입니다.");
        }
        return trimmed;
    }

    private static AiRoadmapStandardItem.Category legacyCategory(RoadmapLane lane, RoadmapItemType type) {
        return switch (lane) {
            case LEARNING -> AiRoadmapStandardItem.Category.COURSE;
            case PROJECT -> AiRoadmapStandardItem.Category.PROJECT;
            case CERTIFICATION -> AiRoadmapStandardItem.Category.CERTIFICATE;
            case EXPERIENCE -> type == RoadmapItemType.INTERNSHIP
                    ? AiRoadmapStandardItem.Category.INTERNSHIP
                    : type == RoadmapItemType.CONTEST || type == RoadmapItemType.HACKATHON
                    ? AiRoadmapStandardItem.Category.CONTEST
                    : AiRoadmapStandardItem.Category.ETC;
        };
    }
}
