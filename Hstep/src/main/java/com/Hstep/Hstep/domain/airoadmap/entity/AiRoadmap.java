package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_roadmap", uniqueConstraints = @UniqueConstraint(name = "uk_ai_roadmap_user", columnNames = "user_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRoadmap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ai_roadmap_id")
    private Long aiRoadmapId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interest_job_id", nullable = false)
    private Job interestJob;

    private AiRoadmap(Member member, Job interestJob) {
        this.member = member;
        this.interestJob = interestJob;
    }

    public static AiRoadmap create(Member member, Job interestJob) {
        return new AiRoadmap(member, interestJob);
    }

    public void changeInterestJob(Job interestJob) {
        this.interestJob = interestJob;
    }
}
