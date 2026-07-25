package com.Hstep.Hstep.domain.profile.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "volunteer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Volunteer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "volunteer_id")
    private Long volunteerId;

    @Column(name = "volunteer_name", nullable = false, length = 100)
    private String volunteerName;

    @Column(name = "volunteer_hours", nullable = false)
    private Integer volunteerHours;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    public Volunteer(String volunteerName, Integer volunteerHours, String description, Member member) {
        this.volunteerName = volunteerName;
        this.volunteerHours = volunteerHours;
        this.description = description;
        this.member = member;
    }

    public void update(String volunteerName, Integer volunteerHours, String description) {
        this.volunteerName = volunteerName;
        this.volunteerHours = volunteerHours;
        this.description = description;
    }
}