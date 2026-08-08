package com.Hstep.Hstep.domain.airoadmap.repository;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapChangeProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiRoadmapChangeProposalRepository extends JpaRepository<AiRoadmapChangeProposal, String> {
    Optional<AiRoadmapChangeProposal> findByProposalIdAndMember_UserId(String proposalId, String userId);
}
