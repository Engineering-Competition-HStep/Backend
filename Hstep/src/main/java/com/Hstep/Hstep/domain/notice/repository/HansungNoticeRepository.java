package com.Hstep.Hstep.domain.notice.repository;

import com.Hstep.Hstep.domain.notice.entity.HansungNotice;
import com.Hstep.Hstep.domain.notice.entity.NoticeSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface HansungNoticeRepository
        extends JpaRepository<HansungNotice, Long>,
        JpaSpecificationExecutor<HansungNotice> {

    List<HansungNotice> findAllBySourceUrlIn(Collection<String> sourceUrls);

    @Query("""
            select distinct n.category
            from HansungNotice n
            where (:source is null or n.source = :source)
            order by n.category asc
            """)
    List<String> findDistinctCategories(@Param("source") NoticeSource source);
}
