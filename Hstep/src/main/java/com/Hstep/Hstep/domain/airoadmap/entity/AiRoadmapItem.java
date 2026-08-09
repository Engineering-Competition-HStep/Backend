package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "standard_item_id", nullable = false)
    private AiRoadmapStandardItem standardItem;

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
        return new AiRoadmapItem(aiRoadmap, standardItem, status, standardItem.getPriority(), aiApplied);
    }

    public void complete(boolean aiApplied) {
        this.status = Status.COMPLETED;
        this.aiApplied = aiApplied;
    }

    public void hide() {
        this.status = Status.HIDDEN;
        this.aiApplied = true;
    }

    public void changePriority(AiRoadmapStandardItem.Priority priority) {
        this.priority = priority;
        this.aiApplied = true;
    }
}
