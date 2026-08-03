package com.Hstep.Hstep.domain.job.entity;

import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "job_track",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_job_track_track_job",
                        columnNames = {"track_id", "job_id"}
                )
        },
        indexes = {
                @Index(name = "idx_job_track_track_id", columnList = "track_id"),
                @Index(name = "idx_job_track_job_id", columnList = "job_id")
        }
)
public class JobTrack extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_track_id")
    private Long jobTrackId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "track_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_job_track_track")
    )
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "job_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_job_track_job")
    )
    private Job job;

    protected JobTrack() {
    }

    private JobTrack(Track track, Job job) {
        this.track = track;
        this.job = job;
    }

    public static JobTrack create(Track track, Job job) {
        if (track == null) {
            throw new IllegalArgumentException("트랙은 필수입니다.");
        }
        if (job == null) {
            throw new IllegalArgumentException("직무는 필수입니다.");
        }
        return new JobTrack(track, job);
    }

    public Long getJobTrackId() {
        return jobTrackId;
    }

    public Track getTrack() {
        return track;
    }

    public Job getJob() {
        return job;
    }
}
