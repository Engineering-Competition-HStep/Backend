package com.Hstep.Hstep.domain.job.entity;

import com.Hstep.Hstep.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "job",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_job_name", columnNames = "job_name")
        },
        indexes = {
                @Index(name = "idx_job_category", columnList = "job_category")
        }
)
public class Job extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_category", nullable = false, length = 50)
    private JobCategory jobCategory;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    protected Job() {
    }

    private Job(String jobName, JobCategory jobCategory, String description) {
        this.jobName = jobName;
        this.jobCategory = jobCategory;
        this.description = description;
    }

    public static Job create(String jobName, JobCategory jobCategory, String description) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("직무 이름은 비어 있을 수 없습니다.");
        }
        if (jobCategory == null) {
            throw new IllegalArgumentException("직무 카테고리는 필수입니다.");
        }
        return new Job(jobName.trim(), jobCategory, description);
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public String getDescription() {
        return description;
    }

    public void update(String jobName, JobCategory jobCategory, String description) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("직무 이름은 비어 있을 수 없습니다.");
        }
        if (jobCategory == null) {
            throw new IllegalArgumentException("직무 카테고리는 필수입니다.");
        }
        this.jobName = jobName.trim();
        this.jobCategory = jobCategory;
        this.description = description;
    }
}
