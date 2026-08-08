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
     * TRACK 초기화 로직이 ApplicationRunner/CommandLineRunner로 실행된 뒤 동작합니다.
     * 기존 JOB/JOB_TRACK 데이터가 있어도 누락된 직무와 연결만 추가하여 여러 번 실행해도 안전하게 유지합니다.
     */
    @Order(200)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
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

        Set<Long> matchedTrackIds = resolvedSeeds.stream()
                .map(resolved -> resolved.track().getTrackId())
                .collect(Collectors.toSet());
        List<String> unmatchedDbTracks = tracks.stream()
                .filter(track -> !matchedTrackIds.contains(track.getTrackId()))
                .map(Track::getTrackName)
                .toList();

        Map<String, JobSeed> uniqueJobSeeds = new LinkedHashMap<>();
        for (ResolvedTrackSeed resolved : resolvedSeeds) {
            for (JobSeed seed : resolved.jobs()) {
                uniqueJobSeeds.putIfAbsent(seed.jobName(), seed);
            }
        }

        Map<String, Job> jobByName = jobRepository.findAllByOrderByJobNameAsc().stream()
                .collect(Collectors.toMap(
                        Job::getJobName,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
        int existingJobCount = jobByName.size();

        List<Job> missingJobs = uniqueJobSeeds.values().stream()
                .filter(seed -> !jobByName.containsKey(seed.jobName()))
                .map(seed -> Job.create(
                        seed.jobName(),
                        seed.category(),
                        createDescription(seed)
                ))
                .toList();

        if (!missingJobs.isEmpty()) {
            for (Job savedJob : jobRepository.saveAllAndFlush(missingJobs)) {
                jobByName.put(savedJob.getJobName(), savedJob);
            }
        }

        Set<String> existingLinkKeys = jobTrackRepository.findAll().stream()
                .map(link -> linkKey(link.getTrack().getTrackId(), link.getJob().getJobId()))
                .collect(Collectors.toSet());

        List<JobTrack> missingLinks = new ArrayList<>();

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

                String linkKey = linkKey(resolved.track().getTrackId(), job.getJobId());
                if (existingLinkKeys.add(linkKey)) {
                    missingLinks.add(JobTrack.create(resolved.track(), job));
                }
            }
        }

        if (!missingLinks.isEmpty()) {
            jobTrackRepository.saveAll(missingLinks);
        }

        log.info(
                "JOB/JOB_TRACK 동기화 완료. matchedTracks={}, existingJobs={}, createdJobs={}, createdJobTrackLinks={}",
                resolvedSeeds.size(),
                existingJobCount,
                missingJobs.size(),
                missingLinks.size()
        );

        if (!unmatchedCatalogTracks.isEmpty()) {
            log.info(
                    "DB에 존재하지 않아 연결하지 않은 JobSeedCatalog 트랙 수={}, tracks={}",
                    unmatchedCatalogTracks.size(),
                    unmatchedCatalogTracks
            );
        }

        if (!unmatchedDbTracks.isEmpty()) {
            log.info(
                    "JobSeedCatalog 직무 연결 대상이 아닌 DB 트랙/조직 수={}, tracks={}",
                    unmatchedDbTracks.size(),
                    unmatchedDbTracks
            );
        }
    }

    private String createDescription(JobSeed seed) {
        return seed.jobName()
                + " 직무로, "
                + seed.category().getDisplayName()
                + " 분야의 관련 업무를 수행합니다.";
    }

    private String linkKey(Long trackId, Long jobId) {
        return trackId + ":" + jobId;
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
