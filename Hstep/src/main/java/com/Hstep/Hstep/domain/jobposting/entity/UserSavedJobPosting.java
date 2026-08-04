package com.Hstep.Hstep.domain.jobposting.entity;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_saved_job_posting",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_saved_job_user_posting",
                        columnNames = {"user_id", "job_posting_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_saved_job_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSavedJobPosting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_id")
    private Long savedId;

    @Column(name = "memo", length = 500)
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    private UserSavedJobPosting(
            Member member,
            JobPosting jobPosting,
            String memo
    ) {
        this.member = member;
        this.jobPosting = jobPosting;
        this.memo = memo;
    }

    public static UserSavedJobPosting create(
            Member member,
            JobPosting jobPosting,
            String memo
    ) {
        return new UserSavedJobPosting(member, jobPosting, memo);
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }
}