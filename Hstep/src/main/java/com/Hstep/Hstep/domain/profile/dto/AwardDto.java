package com.Hstep.Hstep.domain.profile.dto;

import com.Hstep.Hstep.domain.profile.entity.Award;
import java.time.LocalDateTime;

public class AwardDto {

    public record Request(String competitionName, String awardName, Integer awardRank, String description) {
    }

    public record Response(Long awardId, String competitionName, String awardName, Integer awardRank,
                           String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response from(Award award) {
            return new Response(award.getAwardId(), award.getCompetitionName(), award.getAwardName(),
                    award.getAwardRank(), award.getDescription(), award.getCreatedAt(), award.getUpdatedAt());
        }
    }
}