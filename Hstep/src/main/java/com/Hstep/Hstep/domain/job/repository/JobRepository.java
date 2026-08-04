package com.Hstep.Hstep.domain.job.repository;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findByJobName(String jobName);

    boolean existsByJobName(String jobName);

    List<Job> findAllByJobCategoryOrderByJobNameAsc(JobCategory jobCategory);

    List<Job> findAllByOrderByJobNameAsc();
}
