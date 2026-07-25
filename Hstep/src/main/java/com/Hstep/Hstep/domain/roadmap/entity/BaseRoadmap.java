package com.Hstep.Hstep.domain.roadmap.entity;

import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "base_roadmap")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseRoadmap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_id")
    private Long roadmapId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false, unique = true)
    private Track track;

    @OneToMany(mappedBy = "baseRoadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BaseRoadmapItem> items = new ArrayList<>();

    public BaseRoadmap(String title, Track track) {
        this.title = title;
        this.track = track;
    }

    public void update(String title) {
        this.title = title;
    }
}