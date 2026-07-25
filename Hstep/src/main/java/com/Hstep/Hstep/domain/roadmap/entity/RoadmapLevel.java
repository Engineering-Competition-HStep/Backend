package com.Hstep.Hstep.domain.roadmap.entity;

public enum RoadmapLevel {
    BASIC("기초"),
    CORE("핵심"),
    ADVANCED("심화"),
    APPLIED("활용");

    private final String label;

    RoadmapLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}