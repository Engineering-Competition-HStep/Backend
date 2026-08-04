package com.Hstep.Hstep.domain.jobposting.entity;

import com.Hstep.Hstep.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_posting",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_job_posting_source_external",
                        columnNames = {"source_site", "external_id"}
                )
        },
        indexes = {
                @Index(name = "idx_job_posting_deadline", columnList = "deadline"),
                @Index(name = "idx_job_posting_job_role", columnList = "job_role"),
                @Index(name = "idx_job_posting_region", columnList = "region")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_posting_id")
    private Long jobPostingId;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;

    @Column(name = "source_site", nullable = false, length = 30)
    private String sourceSite;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "job_role", length = 255)
    private String jobRole;

    @Column(name = "region", length = 255)
    private String region;

    @Column(name = "employment_type", length = 100)
    private String employmentType;

    @Column(name = "career_condition", length = 100)
    private String careerCondition;

    @Column(name = "education_condition", length = 100)
    private String educationCondition;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "source_url", nullable = false, length = 1000)
    private String sourceUrl;

    private JobPosting(
            String externalId,
            String sourceSite,
            String companyName,
            String title,
            String jobRole,
            String region,
            String employmentType,
            String careerCondition,
            String educationCondition,
            LocalDateTime deadline,
            String sourceUrl
    ) {
        this.externalId = externalId;
        this.sourceSite = sourceSite;
        this.companyName = companyName;
        this.title = title;
        this.jobRole = jobRole;
        this.region = region;
        this.employmentType = employmentType;
        this.careerCondition = careerCondition;
        this.educationCondition = educationCondition;
        this.deadline = deadline;
        this.sourceUrl = sourceUrl;
    }

    public static JobPosting create(
            String externalId,
            String sourceSite,
            String companyName,
            String title,
            String jobRole,
            String region,
            String employmentType,
            String careerCondition,
            String educationCondition,
            LocalDateTime deadline,
            String sourceUrl
    ) {
        return new JobPosting(
                externalId,
                sourceSite,
                companyName,
                title,
                jobRole,
                region,
                employmentType,
                careerCondition,
                educationCondition,
                deadline,
                sourceUrl
        );
    }

    public void updateFromApi(
            String companyName,
            String title,
            String jobRole,
            String region,
            String employmentType,
            String careerCondition,
            String educationCondition,
            LocalDateTime deadline,
            String sourceUrl
    ) {
        this.companyName = companyName;
        this.title = title;
        this.jobRole = jobRole;
        this.region = region;
        this.employmentType = employmentType;
        this.careerCondition = careerCondition;
        this.educationCondition = educationCondition;
        this.deadline = deadline;
        this.sourceUrl = sourceUrl;
    }
}