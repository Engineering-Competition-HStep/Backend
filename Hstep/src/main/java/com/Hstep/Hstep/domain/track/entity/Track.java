package com.Hstep.Hstep.domain.track.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "track",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_track_code",
                        columnNames = "track_code"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id")
    private Long trackId;

    @Column(name = "track_code", nullable = false, length = 100)
    private String trackCode;

    @Column(name = "track_name", nullable = false, length = 100)
    private String trackName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Track(String trackCode, String trackName) {
        this.trackCode = trackCode;
        this.trackName = trackName;
    }

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
