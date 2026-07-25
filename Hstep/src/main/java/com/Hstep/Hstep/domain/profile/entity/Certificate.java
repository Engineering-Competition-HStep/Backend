package com.Hstep.Hstep.domain.profile.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certificate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Long certificateId;

    @Column(name = "certificate_name", nullable = false, length = 100)
    private String certificateName;

    @Column(name = "issued_year", nullable = false)
    private Integer issuedYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    public Certificate(String certificateName, Integer issuedYear, Member member) {
        this.certificateName = certificateName;
        this.issuedYear = issuedYear;
        this.member = member;
    }

    public void update(String certificateName, Integer issuedYear) {
        this.certificateName = certificateName;
        this.issuedYear = issuedYear;
    }
}