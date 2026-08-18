package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapItem;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapItemType;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapLane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoadmapEvidenceMatcher {

    private final AiRoadmapProfileAnalyzer profileAnalyzer;

    public AiRoadmapItem.Status resolve(String userId, AiRoadmapStandardItem standard) {
        RoadmapLane lane = standard.getRoadmapLaneEffective();
        RoadmapItemType type = standard.getItemTypeEffective();
        if (type == RoadmapItemType.JOB_PREPARATION) return AiRoadmapItem.Status.PENDING;
        if (lane == RoadmapLane.CERTIFICATION) return resolveCertificate(userId, standard);

        List<String> evidence = profileAnalyzer.activityEvidence(userId);
        if (evidence.isEmpty()) return AiRoadmapItem.Status.PENDING;
        Set<String> keywords = tokens(standard);
        if (keywords.isEmpty()) return AiRoadmapItem.Status.PENDING;

        int bestMatch = evidence.stream()
                .map(String::toLowerCase)
                .mapToInt(value -> (int) keywords.stream().filter(value::contains).count())
                .max().orElse(0);
        if (bestMatch == 0) return AiRoadmapItem.Status.PENDING;
        if (lane == RoadmapLane.LEARNING) return AiRoadmapItem.Status.NEEDS_IMPROVEMENT;
        if (bestMatch >= Math.min(2, keywords.size())) return AiRoadmapItem.Status.COMPLETED;
        return AiRoadmapItem.Status.NEEDS_IMPROVEMENT;
    }

    private AiRoadmapItem.Status resolveCertificate(String userId, AiRoadmapStandardItem standard) {
        Set<String> certificates = profileAnalyzer.certificateNames(userId);
        String normalizedTitle = normalizeCertificate(standard.getTitle());
        boolean matched = certificates.stream().anyMatch(name -> name.equals(normalizedTitle));
        if (!matched && standard.getKeyword() != null) {
            matched = Arrays.stream(standard.getKeyword().split("[,;/|·]"))
                    .map(RoadmapEvidenceMatcher::normalizeCertificate)
                    .filter(value -> value.length() >= 2)
                    .anyMatch(certificates::contains);
        }
        return matched ? AiRoadmapItem.Status.COMPLETED : AiRoadmapItem.Status.PENDING;
    }

    private Set<String> tokens(AiRoadmapStandardItem standard) {
        String value = standard.getTitle() + " " + (standard.getKeyword() == null ? "" : standard.getKeyword());
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("[\\s,;/|·]+"))
                .map(String::trim)
                .filter(token -> token.length() >= 2)
                .filter(token -> !Set.of("준비", "학습", "프로젝트", "활동", "기초", "관련").contains(token))
                .collect(Collectors.toSet());
    }

    private static String normalizeCertificate(String value) {
        return AiRoadmapProfileAnalyzer.normalizeEvidence(value)
                .replace("자격증", "").replace("준비", "").replace("취득", "");
    }
}
