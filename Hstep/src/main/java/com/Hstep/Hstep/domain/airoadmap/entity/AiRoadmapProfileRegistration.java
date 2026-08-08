package com.Hstep.Hstep.domain.airoadmap.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_roadmap_profile_registration", uniqueConstraints = @UniqueConstraint(
        name = "uk_ai_profile_registration_user", columnNames = "user_id"
))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRoadmapProfileRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_registration_id")
    private Long profileRegistrationId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(name = "certificate_none", nullable = false)
    private boolean certificateNone;

    @Column(name = "award_none", nullable = false)
    private boolean awardNone;

    @Column(name = "volunteer_none", nullable = false)
    private boolean volunteerNone;

    @Column(name = "extra_activity_none", nullable = false)
    private boolean extraActivityNone;

    private AiRoadmapProfileRegistration(Member member) {
        this.member = member;
    }

    public static AiRoadmapProfileRegistration create(Member member) {
        return new AiRoadmapProfileRegistration(member);
    }

    public void update(boolean certificateNone, boolean awardNone, boolean volunteerNone, boolean extraActivityNone) {
        this.certificateNone = certificateNone;
        this.awardNone = awardNone;
        this.volunteerNone = volunteerNone;
        this.extraActivityNone = extraActivityNone;
    }
}
