package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;

public interface AiRoadmapIntentClassifier {
    AiRoadmapChangeProposal.ActionType classify(String message);
}
