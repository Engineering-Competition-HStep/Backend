package com.Hstep.Hstep.domain.notice.service;

import com.Hstep.Hstep.domain.notice.dto.NoticePageResponse;
import com.Hstep.Hstep.domain.notice.dto.NoticeResponse;
import com.Hstep.Hstep.domain.notice.dto.NoticeSearchCondition;
import com.Hstep.Hstep.domain.notice.entity.HansungNotice;
import com.Hstep.Hstep.domain.notice.entity.NoticeSource;
import com.Hstep.Hstep.domain.notice.exception.NoticeNotFoundException;
import com.Hstep.Hstep.domain.notice.repository.HansungNoticeRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class NoticeQueryService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of(
                    "publishedAt",
                    "createdAt",
                    "updatedAt",
                    "title",
                    "category"
            );

    private final HansungNoticeRepository noticeRepository;

    public NoticeQueryService(
            HansungNoticeRepository noticeRepository
    ) {
        this.noticeRepository = noticeRepository;
    }

    public NoticePageResponse<NoticeResponse> search(
            NoticeSearchCondition condition,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        validateDateRange(
                condition.startDate(),
                condition.endDate()
        );

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));

        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy)
                ? sortBy
                : "publishedAt";

        Sort.Direction sortDirection =
                "asc".equalsIgnoreCase(direction)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(sortDirection, safeSortBy)
                        .and(Sort.by(
                                Sort.Direction.DESC,
                                "noticeId"
                        ))
        );

        Specification<HansungNotice> specification =
                buildSpecification(condition);

        Page<NoticeResponse> result =
                noticeRepository
                        .findAll(specification, pageable)
                        .map(NoticeResponse::from);

        return NoticePageResponse.from(result);
    }

    public NoticeResponse getById(Long noticeId) {
        HansungNotice notice = noticeRepository
                .findById(noticeId)
                .orElseThrow(
                        () -> new NoticeNotFoundException(noticeId)
                );

        return NoticeResponse.from(notice);
    }

    public List<NoticeResponse> getLatest(
            NoticeSource source,
            int size
    ) {
        int safeSize = Math.max(1, Math.min(size, 20));

        Specification<HansungNotice> specification =
                buildSpecification(
                        new NoticeSearchCondition(
                                null,
                                null,
                                source,
                                null,
                                null
                        )
                );

        Pageable pageable = PageRequest.of(
                0,
                safeSize,
                Sort.by(
                        Sort.Direction.DESC,
                        "publishedAt"
                ).and(Sort.by(
                        Sort.Direction.DESC,
                        "noticeId"
                ))
        );

        return noticeRepository
                .findAll(specification, pageable)
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    public List<String> getCategories(
            NoticeSource source
    ) {
        return noticeRepository.findDistinctCategories(source);
    }

    private Specification<HansungNotice> buildSpecification(
            NoticeSearchCondition condition
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (StringUtils.hasText(condition.keyword())) {
                String keyword =
                        "%"
                                + condition.keyword()
                                .trim()
                                .toLowerCase(Locale.ROOT)
                                + "%";

                Predicate titleLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        keyword
                );
                Predicate categoryLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("category")),
                        keyword
                );

                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.or(
                                titleLike,
                                categoryLike
                        )
                );
            }

            if (StringUtils.hasText(condition.category())) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                root.get("category"),
                                condition.category().trim()
                        )
                );
            }

            if (condition.source() != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                root.get("source"),
                                condition.source()
                        )
                );
            }

            if (condition.startDate() != null) {
                LocalDateTime start =
                        condition.startDate().atStartOfDay();

                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("publishedAt"),
                                start
                        )
                );
            }

            if (condition.endDate() != null) {
                LocalDateTime exclusiveEnd =
                        condition.endDate()
                                .plusDays(1)
                                .atStartOfDay();

                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThan(
                                root.get("publishedAt"),
                                exclusiveEnd
                        )
                );
            }

            return predicate;
        };
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate는 endDate보다 늦을 수 없습니다."
            );
        }
    }
}
