package com.Hstep.Hstep.domain.notice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "hansung_notice",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hansung_notice_source_url",
                        columnNames = "source_url"
                )
        },
        indexes = {
                @Index(
                        name = "idx_hansung_notice_published_at",
                        columnList = "published_at"
                ),
                @Index(
                        name = "idx_hansung_notice_category",
                        columnList = "category"
                ),
                @Index(
                        name = "idx_hansung_notice_source",
                        columnList = "source"
                )
        }
)
public class HansungNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private NoticeSource source;

    @Column(name = "source_url", nullable = false, length = 500)
    private String sourceUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    protected HansungNotice() {
    }

    private HansungNotice(
            String title,
            String category,
            NoticeSource source,
            String sourceUrl,
            LocalDateTime publishedAt
    ) {
        this.title = title;
        this.category = category;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.publishedAt = publishedAt;
    }

    public static HansungNotice create(
            String title,
            String category,
            NoticeSource source,
            String sourceUrl,
            LocalDateTime publishedAt
    ) {
        return new HansungNotice(
                title,
                category,
                source,
                sourceUrl,
                publishedAt
        );
    }

    public boolean updateFrom(
            String title,
            String category,
            NoticeSource source,
            LocalDateTime publishedAt
    ) {
        boolean changed = false;

        if (!Objects.equals(this.title, title)) {
            this.title = title;
            changed = true;
        }
        if (!Objects.equals(this.category, category)) {
            this.category = category;
            changed = true;
        }
        if (!Objects.equals(this.source, source)) {
            this.source = source;
            changed = true;
        }
        if (!Objects.equals(this.publishedAt, publishedAt)) {
            this.publishedAt = publishedAt;
            changed = true;
        }

        return changed;
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

    public Long getNoticeId() {
        return noticeId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public NoticeSource getSource() {
        return source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
