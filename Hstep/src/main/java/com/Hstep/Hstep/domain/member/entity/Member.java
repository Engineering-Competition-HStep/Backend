package com.Hstep.Hstep.domain.member.entity;

import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "grade", nullable = false)
    private Integer grade;

    @Column(name = "gpa", precision = 3, scale = 2)
    private BigDecimal gpa;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    @OneToMany(
            mappedBy = "member",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("trackOrder ASC")
    private List<MemberTrack> memberTracks = new ArrayList<>();

    private Member(
            String userId,
            String email,
            String encodedPassword,
            String name,
            Integer grade,
            MemberRole role
    ) {
        this.userId = userId;
        this.email = email;
        this.password = encodedPassword;
        this.name = name;
        this.grade = grade;
        this.role = role;
    }

    public static Member create(
            String userId,
            String email,
            String encodedPassword,
            String name,
            Integer grade
    ) {
        return new Member(userId, email, encodedPassword, name, grade, MemberRole.USER);
    }

    public void updateProfile(String name, Integer grade, BigDecimal gpa) {
        if (name != null) {
            this.name = name;
        }
        if (grade != null) {
            this.grade = grade;
        }
        if (gpa != null) {
            this.gpa = gpa;
        }
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void replaceTracks(List<Long> trackIds) {
        memberTracks.clear();

        for (int index = 0; index < trackIds.size(); index++) {
            memberTracks.add(MemberTrack.create(this, trackIds.get(index), index + 1));
        }
    }

    public void updateGpa(BigDecimal gpa) {
        this.gpa = gpa;
    }
}
