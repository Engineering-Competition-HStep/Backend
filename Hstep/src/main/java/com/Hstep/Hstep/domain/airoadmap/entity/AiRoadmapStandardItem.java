package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_roadmap_standard_item", indexes = {
        @Index(name = "idx_ai_standard_job", columnList = "job_id"),
        @Index(name = "idx_ai_standard_job_grade_category", columnList = "job_id,target_grade,category")
})
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

    private AiRoadmapStandardItem(Job job, Category category, Integer targetGrade, Priority priority,
                                  Integer displayOrder, String title, String description, String keyword,
                                  String recommendationReason, String externalUrl, boolean requiredItem) {
        this.job = job;
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

    public static AiRoadmapStandardItem create(Job job, Category category, Integer targetGrade,
                                                Priority priority, Integer displayOrder, String title,
                                                String description, String keyword, String recommendationReason,
                                                String externalUrl, boolean requiredItem) {
        return new AiRoadmapStandardItem(job, category, targetGrade, priority, displayOrder, title,
                description, keyword, recommendationReason, externalUrl, requiredItem);
    }

    public void update(Category category, Integer targetGrade, Priority priority, Integer displayOrder,
                       String title, String description, String keyword, String recommendationReason,
                       String externalUrl, boolean requiredItem) {
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
}
