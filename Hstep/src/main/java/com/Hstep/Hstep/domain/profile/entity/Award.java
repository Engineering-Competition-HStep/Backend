package com.Hstep.Hstep.domain.profile.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "award")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Award extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "award_id")
    private Long awardId;

    @Column(name = "competition_name", nullable = false, length = 100)
    private String competitionName;

    @Column(name = "award_name", nullable = false, length = 100)
    private String awardName;

    @Column(name = "award_rank")
    private Integer awardRank;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    public Award(String competitionName, String awardName, Integer awardRank, String description, Member member) {
        this.competitionName = competitionName;
        this.awardName = awardName;
        this.awardRank = awardRank;
        this.description = description;
        this.member = member;
    }

    public void update(String competitionName, String awardName, Integer awardRank, String description) {
        this.competitionName = competitionName;
        this.awardName = awardName;
        this.awardRank = awardRank;
        this.description = description;
    }
}