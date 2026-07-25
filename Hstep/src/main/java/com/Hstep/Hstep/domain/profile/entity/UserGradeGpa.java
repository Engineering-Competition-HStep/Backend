package com.Hstep.Hstep.domain.profile.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "user_grade_gpa", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "grade"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGradeGpa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_grade_gpa_id")
    private Long userGradeGpaId;

    @Column(name = "grade", nullable = false)
    private Integer grade;

    @Column(name = "gpa", nullable = false)
    private BigDecimal gpa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    public UserGradeGpa(Integer grade, BigDecimal gpa, Member member) {
        this.grade = grade;
        this.gpa = gpa;
        this.member = member;
    }

    public void update(BigDecimal gpa) {
        this.gpa = gpa;
    }
}