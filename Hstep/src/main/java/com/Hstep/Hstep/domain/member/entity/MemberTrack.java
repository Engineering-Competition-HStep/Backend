package com.Hstep.Hstep.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_track",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_track_user_track",
                        columnNames = {"user_id", "track_id"}
                ),
                @UniqueConstraint(
                        name = "uk_user_track_user_order",
                        columnNames = {"user_id", "track_order"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_track_id")
    private Long userTrackId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @Column(name = "track_order", nullable = false)
    private Integer trackOrder;

    private MemberTrack(Member member, Long trackId, Integer trackOrder) {
        this.member = member;
        this.trackId = trackId;
        this.trackOrder = trackOrder;
    }

    public static MemberTrack create(Member member, Long trackId, Integer trackOrder) {
        return new MemberTrack(member, trackId, trackOrder);
    }
}
