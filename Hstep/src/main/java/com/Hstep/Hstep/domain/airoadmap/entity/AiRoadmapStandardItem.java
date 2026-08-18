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
