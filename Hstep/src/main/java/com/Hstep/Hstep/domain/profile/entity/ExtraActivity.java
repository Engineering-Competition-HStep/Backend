package com.Hstep.Hstep.domain.profile.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "extra_activity")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtraActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @Column(name = "activity_name", nullable = false, length = 100)
    private String activityName;

    @Column(name = "field_keyword", length = 100)
    private String fieldKeyword;

    @Column(name = "period", length = 50)
    private String period;

    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    public ExtraActivity(String activityName, String fieldKeyword, String period, String description, Member member) {
        this.activityName = activityName;
        this.fieldKeyword = fieldKeyword;
        this.period = period;
        this.description = description;
        this.member = member;
    }

    public void update(String activityName, String fieldKeyword, String period, String description) {
        this.activityName = activityName;
        this.fieldKeyword = fieldKeyword;
        this.period = period;
        this.description = description;
    }
}