package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "ai_roadmap_standard_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_standard_job_seed",
                        columnNames = {"job_id", "seed_key"}
                )
        },
        indexes = {
                @Index(name = "idx_ai_standard_job", columnList = "job_id"),
                @Index(name = "idx_ai_standard_job_grade_category", columnList = "job_id,target_grade,category")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRoadmapStandardItem extends BaseEntity {

    public enum Category { CONTEST, PROJECT, CERTIFICATE, INTERNSHIP, COURSE, ETC }
    public enum Priority { HIGH, MEDIUM, LOW }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standard_item_id")
    private Long standardItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * 서버 시작 시 생성되는 표준 항목의 안정적인 식별자입니다.
     * 관리자 API로 직접 등록한 항목은 null이며, 동일 직무 안에서는 중복될 수 없습니다.
     */
    @Column(name = "seed_key", length = 80)
    private String seedKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private Category category;

    @Column(name = "target_grade", nullable = false)
    private Integer targetGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "keyword", length = 300)
    private String keyword;

    @Column(name = "recommendation_reason", length = 500)
    private String recommendationReason;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @Column(name = "required_item", nullable = false)
    private boolean requiredItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "roadmap_lane", length = 30)
    private RoadmapLane roadmapLane;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 40)
    private RoadmapItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_stage", length = 30)
    private RoadmapStage targetStage;

    @Column(name = "core_item")
    private Boolean coreItem;

    @Column(name = "default_included")
    private Boolean defaultIncluded;

    @Column(name = "template_version")
    private Integer templateVersion;

    private AiRoadmapStandardItem(
            Job job,
            String seedKey,
            Category category,
            Integer targetGrade,
            Priority priority,
            Integer displayOrder,
            String title,
            String description,
            String keyword,
            String recommendationReason,
            String externalUrl,
            boolean requiredItem
    ) {
        this.job = job;
        this.seedKey = seedKey;
        this.category = category;
        this.targetGrade = targetGrade;
        this.priority = priority;
        this.displayOrder = displayOrder;
        this.title = title;
        this.description = description;
        this.keyword = keyword;
        this.recommendationReason = recommendationReason;
        this.externalUrl = externalUrl;
        this.requiredItem = requiredItem;
        this.roadmapLane = inferLane(category);
        this.itemType = inferItemType(category);
        this.targetStage = RoadmapStage.fromGrade(targetGrade);
        this.coreItem = requiredItem;
        this.defaultIncluded = requiredItem;
    }

    public static AiRoadmapStandardItem create(
            Job job,
            Category category,
            Integer targetGrade,
            Priority priority,
            Integer displayOrder,
            String title,
            String description,
            String keyword,
            String recommendationReason,
            String externalUrl,
            boolean requiredItem
    ) {
        return new AiRoadmapStandardItem(
                job,
                null,
                category,
                targetGrade,
                priority,
                displayOrder,
                title,
                description,
                keyword,
                recommendationReason,
                externalUrl,
                requiredItem
        );
    }

    public static AiRoadmapStandardItem createSeeded(
            Job job,
            String seedKey,
            Category category,
            Integer targetGrade,
            Priority priority,
            Integer displayOrder,
            String title,
            String description,
            String keyword,
            String recommendationReason,
            String externalUrl,
            boolean requiredItem
    ) {
        if (seedKey == null || seedKey.isBlank()) {
            throw new IllegalArgumentException("표준 로드맵 seedKey는 비어 있을 수 없습니다.");
        }

        return new AiRoadmapStandardItem(
                job,
                seedKey.trim(),
                category,
                targetGrade,
                priority,
                displayOrder,
                title,
                description,
                keyword,
                recommendationReason,
                externalUrl,
                requiredItem
        );
    }

    public static AiRoadmapStandardItem createSeeded(
            Job job,
            String seedKey,
            Category category,
            Integer targetGrade,
            Priority priority,
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
            int templateVersion
    ) {
        AiRoadmapStandardItem item = createSeeded(job, seedKey, category, targetGrade, priority,
                displayOrder, title, description, keyword, recommendationReason, externalUrl, requiredItem);
        item.applyTemplateMetadata(roadmapLane, itemType, targetStage, coreItem, defaultIncluded, templateVersion);
        return item;
    }

    public void update(
            Category category,
            Integer targetGrade,
            Priority priority,
            Integer displayOrder,
            String title,
            String description,
            String keyword,
            String recommendationReason,
            String externalUrl,
            boolean requiredItem
    ) {
        this.category = category;
        this.targetGrade = targetGrade;
        this.priority = priority;
        this.displayOrder = displayOrder;
        this.title = title;
        this.description = description;
        this.keyword = keyword;
        this.recommendationReason = recommendationReason;
        this.externalUrl = externalUrl;
        this.requiredItem = requiredItem;
    }

    public void update(
            Category category,
            Integer targetGrade,
            Priority priority,
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
            Boolean coreItem,
            Boolean defaultIncluded
    ) {
        update(category, targetGrade, priority, displayOrder, title, description, keyword,
                recommendationReason, externalUrl, requiredItem);
        this.roadmapLane = roadmapLane != null ? roadmapLane : inferLane(category);
        this.itemType = itemType != null ? itemType : inferItemType(category);
        this.targetStage = targetStage != null ? targetStage : RoadmapStage.fromGrade(targetGrade);
        this.coreItem = coreItem != null ? coreItem : requiredItem;
        this.defaultIncluded = defaultIncluded != null ? defaultIncluded : requiredItem;
    }

    public void synchronizeTemplate(
            Category category,
            Integer targetGrade,
            Priority priority,
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
            int templateVersion
    ) {
        update(category, targetGrade, priority, displayOrder, title, description, keyword,
                recommendationReason, externalUrl, requiredItem, roadmapLane, itemType, targetStage,
                coreItem, defaultIncluded);
        this.templateVersion = templateVersion;
    }

    public boolean isCoreItemEffective() {
        return coreItem != null ? coreItem : requiredItem;
    }

    public boolean isDefaultIncludedEffective() {
        return defaultIncluded != null ? defaultIncluded : requiredItem;
    }

    public RoadmapLane getRoadmapLaneEffective() {
        return roadmapLane != null ? roadmapLane : inferLane(category);
    }

    public RoadmapItemType getItemTypeEffective() {
        return itemType != null ? itemType : inferItemType(category);
    }

    public RoadmapStage getTargetStageEffective() {
        return targetStage != null ? targetStage : RoadmapStage.fromGrade(targetGrade);
    }

    public void backfillLegacyMetadata() {
        if (roadmapLane == null) roadmapLane = inferLane(category);
        if (itemType == null) itemType = inferItemType(category);
        if (targetStage == null) targetStage = RoadmapStage.fromGrade(targetGrade);
        if (coreItem == null) coreItem = requiredItem;
        if (defaultIncluded == null) defaultIncluded = requiredItem;
    }

    private void applyTemplateMetadata(RoadmapLane roadmapLane, RoadmapItemType itemType,
                                       RoadmapStage targetStage, boolean coreItem,
                                       boolean defaultIncluded, int templateVersion) {
        this.roadmapLane = roadmapLane;
        this.itemType = itemType;
        this.targetStage = targetStage;
        this.coreItem = coreItem;
        this.defaultIncluded = defaultIncluded;
        this.templateVersion = templateVersion;
    }

    public static RoadmapLane inferLane(Category category) {
        return switch (category) {
            case COURSE -> RoadmapLane.LEARNING;
            case PROJECT -> RoadmapLane.PROJECT;
            case CERTIFICATE -> RoadmapLane.CERTIFICATION;
            case CONTEST, INTERNSHIP -> RoadmapLane.EXPERIENCE;
            case ETC -> RoadmapLane.EXPERIENCE;
        };
    }

    public static RoadmapItemType inferItemType(Category category) {
        return switch (category) {
            case COURSE -> RoadmapItemType.OTHER;
            case PROJECT -> RoadmapItemType.PORTFOLIO_PROJECT;
            case CERTIFICATE -> RoadmapItemType.CERTIFICATE;
            case CONTEST -> RoadmapItemType.CONTEST;
            case INTERNSHIP -> RoadmapItemType.INTERNSHIP;
            case ETC -> RoadmapItemType.OTHER;
        };
    }

    /**
     * 이전에 수동으로 삽입된 동일 항목을 초기 데이터로 승격할 때만 사용합니다.
     * 한 번 지정된 seedKey는 다른 값으로 변경할 수 없습니다.
     */
    public void assignSeedKeyIfAbsent(String seedKey) {
        if (seedKey == null || seedKey.isBlank()) {
            throw new IllegalArgumentException("표준 로드맵 seedKey는 비어 있을 수 없습니다.");
        }

        String normalizedSeedKey = seedKey.trim();
        if (this.seedKey == null) {
            this.seedKey = normalizedSeedKey;
            return;
        }

        if (!this.seedKey.equals(normalizedSeedKey)) {
            throw new IllegalStateException(
                    "이미 다른 seedKey가 지정된 표준 로드맵 항목입니다. current="
                            + this.seedKey + ", requested=" + normalizedSeedKey
            );
        }
    }
}
