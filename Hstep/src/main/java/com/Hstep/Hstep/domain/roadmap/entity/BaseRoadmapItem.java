package com.Hstep.Hstep.domain.roadmap.entity;

import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "base_roadmap_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseRoadmapItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private BaseRoadmap baseRoadmap;

    public BaseRoadmapItem(Integer itemOrder, String title, Integer grade, String description, BaseRoadmap baseRoadmap) {
        this.itemOrder = itemOrder;
        this.title = title;
        this.grade = grade;
        this.description = description;
        this.baseRoadmap = baseRoadmap;
    }

    public void update(Integer itemOrder, String title, Integer grade, String description) {
        this.itemOrder = itemOrder;
        this.title = title;
        this.grade = grade;
        this.description = description;
    }
}