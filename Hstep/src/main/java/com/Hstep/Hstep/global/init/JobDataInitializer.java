package com.Hstep.Hstep.global.init;

import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobTrack;
import com.Hstep.Hstep.domain.job.repository.JobRepository;
import com.Hstep.Hstep.domain.job.repository.JobTrackRepository;
import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
import com.Hstep.Hstep.global.init.JobSeedCatalog.JobSeed;
import com.Hstep.Hstep.global.init.JobSeedCatalog.TrackJobSeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JobDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(JobDataInitializer.class);

    private final JobRepository jobRepository;
    private final JobTrackRepository jobTrackRepository;
    private final TrackRepository trackRepository;

    public JobDataInitializer(
            JobRepository jobRepository,
            JobTrackRepository jobTrackRepository,
            TrackRepository trackRepository
    ) {
        this.jobRepository = jobRepository;
        this.jobTrackRepository = jobTrackRepository;
        this.trackRepository = trackRepository;
    }

    /**
     * TRACK 초기화 로직이 ApplicationRunner/CommandLineRunner로 실행된 뒤 동작하도록
     * ApplicationReadyEvent 시점에 실행합니다.
     *
     * JOB 테이블에 데이터가 하나라도 있으면 아무 작업도 하지 않습니다.
     */
    @Order(200)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        long existingJobCount = jobRepository.count();

        if (existingJobCount > 0) {
            log.info("JOB 초기 데이터를 건너뜁니다. existingJobCount={}", existingJobCount);
            return;
        }

        List<Track> tracks = trackRepository.findAll();

        if (tracks.isEmpty()) {
            log.warn("TRACK 데이터가 없어 JOB/JOB_TRACK 초기화를 수행하지 않았습니다.");
            return;
        }

        Map<String, Track> trackByNormalizedName = tracks.stream()
                .collect(Collectors.toMap(
                        track -> normalizeTrackName(track.getTrackName()),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        List<ResolvedTrackSeed> resolvedSeeds = new ArrayList<>();
        List<String> unmatchedCatalogTracks = new ArrayList<>();

        for (TrackJobSeed trackSeed : JobSeedCatalog.all()) {
            Track track = trackByNormalizedName.get(normalizeTrackName(trackSeed.trackName()));

            if (track == null) {
                unmatchedCatalogTracks.add(trackSeed.trackName());
                continue;
            }

            resolvedSeeds.add(new ResolvedTrackSeed(track, trackSeed.jobs()));
        }

        if (resolvedSeeds.isEmpty()) {
            log.warn(
                    "DB의 TRACK 이름과 JobSeedCatalog의 트랙 이름이 하나도 일치하지 않아 초기화를 중단합니다. dbTracks={}",
                    tracks.stream().map(Track::getTrackName).toList()
            );
            return;
        }

        /*
         * 같은 직무가 여러 트랙에 연결될 수 있으므로 JOB에는 한 번만 저장합니다.
         * 같은 직무가 서로 다른 카테고리로 선언된 경우 카탈로그에서 먼저 등장한 값을 사용합니다.
         */
        Map<String, JobSeed> uniqueJobSeeds = new LinkedHashMap<>();

        for (ResolvedTrackSeed resolved : resolvedSeeds) {
            for (JobSeed seed : resolved.jobs()) {
                uniqueJobSeeds.putIfAbsent(seed.jobName(), seed);
            }
        }

        List<Job> jobs = uniqueJobSeeds.values().stream()
                .map(seed -> Job.create(
                        seed.jobName(),
                        seed.category(),
                        createDescription(seed)
                ))
                .toList();

        List<Job> savedJobs = jobRepository.saveAllAndFlush(jobs);

        Map<String, Job> jobByName = savedJobs.stream()
                .collect(Collectors.toMap(
                        Job::getJobName,
                        Function.identity(),
                        (first, ignored) -> first
                ));

        List<JobTrack> links = new ArrayList<>();

        for (ResolvedTrackSeed resolved : resolvedSeeds) {
            Set<String> duplicatedLinkGuard = new HashSet<>();

            for (JobSeed seed : resolved.jobs()) {
                if (!duplicatedLinkGuard.add(seed.jobName())) {
                    continue;
                }

                Job job = jobByName.get(seed.jobName());

                if (job == null) {
                    throw new IllegalStateException(
                            "저장된 직무를 찾지 못했습니다. jobName=" + seed.jobName()
                    );
                }

                links.add(JobTrack.create(resolved.track(), job));
            }
        }

        jobTrackRepository.saveAll(links);

        log.info(
                "JOB/JOB_TRACK 초기화 완료. matchedTracks={}, jobs={}, jobTrackLinks={}",
                resolvedSeeds.size(),
                savedJobs.size(),
                links.size()
        );

        if (!unmatchedCatalogTracks.isEmpty()) {
            log.info(
                    "DB에 존재하지 않아 연결하지 않은 카탈로그 트랙 수={}, tracks={}",
                    unmatchedCatalogTracks.size(),
                    unmatchedCatalogTracks
            );
        }
    }

    private String createDescription(JobSeed seed) {
        return seed.jobName()
                + " 직무로, "
                + seed.category().getDisplayName()
                + " 분야의 관련 업무를 수행합니다.";
    }

    /**
     * DB 트랙명이 '웹공학', '웹공학트랙', '웹 공학 트랙' 등으로 조금 달라도 연결되도록 정규화합니다.
     */
    private String normalizeTrackName(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s·ㆍ・&/\\\\_\\-()]", "");

        return normalized.replaceFirst("(트랙|전공|학과)$", "");
    }

    private record ResolvedTrackSeed(Track track, List<JobSeed> jobs) {
    }
}
