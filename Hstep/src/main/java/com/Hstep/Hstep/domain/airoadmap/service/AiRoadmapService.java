package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmap;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapItem;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.exception.AiRoadmapResponseCode;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapItemRepository;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapRepository;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapStandardItemRepository;
import com.Hstep.Hstep.domain.job.entity.Job;
import com.Hstep.Hstep.domain.job.entity.JobTrack;
import com.Hstep.Hstep.domain.job.repository.JobRepository;
import com.Hstep.Hstep.domain.job.repository.JobTrackRepository;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.entity.MemberTrack;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiRoadmapService {

    private final AiRoadmapRepository aiRoadmapRepository;
    private final AiRoadmapItemRepository aiRoadmapItemRepository;
    private final AiRoadmapStandardItemRepository standardItemRepository;
    private final JobRepository jobRepository;
    private final JobTrackRepository jobTrackRepository;
    private final AiRoadmapProfileAnalyzer profileAnalyzer;

    public AiRoadmapDto.EligibilityResponse checkEligibility(String userId) {
        return profileAnalyzer.checkEligibility(userId);
    }

    @Transactional
    public void updateProfileRegistration(String userId, AiRoadmapDto.ProfileRegistrationRequest request) {
        profileAnalyzer.updateRegistration(userId, request);
    }

    public List<AiRoadmapDto.JobRecommendationResponse> recommendJobs(String userId) {
        requireEligible(userId);
        Member member = profileAnalyzer.getMemberWithTracks(userId);
        String corpus = profileAnalyzer.buildProfileCorpus(userId);

        Map<Long, Job> candidates = new LinkedHashMap<>();
        Set<Long> memberTrackIds = member.getMemberTracks().stream()
                .map(MemberTrack::getTrackId)
                .collect(Collectors.toSet());

        for (Long trackId : memberTrackIds) {
            for (JobTrack relation : jobTrackRepository.findAllByTrack_TrackIdOrderByJob_JobNameAsc(trackId)) {
                candidates.putIfAbsent(relation.getJob().getJobId(), relation.getJob());
            }
        }

        if (candidates.isEmpty()) {
            throw new BaseException(AiRoadmapResponseCode.RECOMMENDATION_NOT_FOUND);
        }

        return candidates.values().stream()
                .map(job -> scoreJob(member, memberTrackIds, corpus, job))
                .sorted(Comparator.comparingInt(AiRoadmapDto.JobRecommendationResponse::score).reversed()
                        .thenComparing(AiRoadmapDto.JobRecommendationResponse::jobName))
                .limit(3)
                .toList();
    }

    private AiRoadmapDto.JobRecommendationResponse scoreJob(Member member, Set<Long> trackIds,
                                                              String corpus, Job job) {
        boolean trackMatched = jobTrackRepository.findAllByJob_JobIdOrderByTrack_TrackNameAsc(job.getJobId()).stream()
                .anyMatch(relation -> trackIds.contains(relation.getTrack().getTrackId()));
        int trackScore = trackMatched ? 40 : 0;

        List<AiRoadmapStandardItem> standards = standardItemRepository
                .findAllByJob_JobIdOrderByTargetGradeAscDisplayOrderAsc(job.getJobId());
        List<AiRoadmapStandardItem> specItems = standards.stream()
                .filter(item -> item.getCategory() != AiRoadmapStandardItem.Category.COURSE)
                .toList();
        List<AiRoadmapStandardItem> courseItems = standards.stream()
                .filter(item -> item.getCategory() == AiRoadmapStandardItem.Category.COURSE)
                .toList();

        int specMatched = (int) specItems.stream().filter(item -> matchRatio(item, corpus) > 0).count();
        int courseMatched = (int) courseItems.stream().filter(item -> matchRatio(item, corpus) > 0).count();
        int specScore = proportionalScore(specMatched, specItems.size(), 30);
        int courseScore = proportionalScore(courseMatched, courseItems.size(), 20);
        int gradeScore = standards.stream().anyMatch(item -> Objects.equals(item.getTargetGrade(), member.getGrade())) ? 10 : 0;
        int total = trackScore + specScore + courseScore + gradeScore;

        List<String> reasons = new ArrayList<>();
        if (trackScore > 0) reasons.add("현재 소속 트랙과 직접 연결된 직무입니다.");
        if (specScore > 0) reasons.add("등록한 개인 스펙과 직무 준비 항목의 키워드 연관성이 있습니다.");
        if (courseScore > 0) reasons.add("등록한 학습 경험이 직무 관련 수업 기준과 연관됩니다.");
        if (gradeScore > 0) reasons.add("현재 학년에서 준비할 수 있는 표준 로드맵 항목이 있습니다.");
        if (reasons.isEmpty()) reasons.add("현재 등록된 트랙 기준으로 추천된 직무입니다.");

        return new AiRoadmapDto.JobRecommendationResponse(job.getJobId(), job.getJobName(), total,
                trackScore, specScore, courseScore, gradeScore, reasons);
    }

    private int proportionalScore(int matched, int total, int maxScore) {
        if (total == 0 || matched == 0) return 0;
        return Math.min(maxScore, (int) Math.round((double) matched / total * maxScore));
    }

    @Transactional
    public AiRoadmapDto.RoadmapResponse createOrReplace(String userId, Long jobId) {
        requireEligible(userId);
        List<Long> recommendedIds = recommendJobs(userId).stream()
                .map(AiRoadmapDto.JobRecommendationResponse::jobId)
                .toList();
        if (!recommendedIds.contains(jobId)) {
            throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }

        Member member = profileAnalyzer.getMemberWithTracks(userId);
        Job job = findJob(jobId);
        List<AiRoadmapStandardItem> standards = standardItemRepository
                .findAllByJob_JobIdOrderByTargetGradeAscDisplayOrderAsc(jobId);
        if (standards.isEmpty()) {
            throw new BaseException(AiRoadmapResponseCode.STANDARD_ROADMAP_NOT_FOUND);
        }

        AiRoadmap roadmap = aiRoadmapRepository.findByMember_UserId(userId)
                .orElseGet(() -> aiRoadmapRepository.save(AiRoadmap.create(member, job)));
        if (!Objects.equals(roadmap.getInterestJob().getJobId(), jobId)) {
            roadmap.changeInterestJob(job);
            aiRoadmapItemRepository.deleteAllByAiRoadmap_AiRoadmapId(roadmap.getAiRoadmapId());
        } else if (!aiRoadmapItemRepository
                .findAllByAiRoadmap_AiRoadmapIdOrderByStandardItem_TargetGradeAscStandardItem_DisplayOrderAsc(roadmap.getAiRoadmapId())
                .isEmpty()) {
            return toRoadmapResponse(member, roadmap, null, null);
        }

        String corpus = profileAnalyzer.buildProfileCorpus(userId);
        List<AiRoadmapItem> items = standards.stream()
                .filter(AiRoadmapStandardItem::isRequiredItem)
                .map(standard -> AiRoadmapItem.create(roadmap, standard, resolveStatus(standard, corpus), false))
                .toList();
        aiRoadmapItemRepository.saveAll(items);
        return toRoadmapResponse(member, roadmap, null, null);
    }

    public AiRoadmapDto.RoadmapResponse getMyRoadmap(String userId, Integer grade,
                                                       AiRoadmapStandardItem.Category category) {
        requireEligible(userId);
        Member member = profileAnalyzer.getMemberWithTracks(userId);
        AiRoadmap roadmap = findRoadmap(userId);
        return toRoadmapResponse(member, roadmap, grade, category);
    }

    @Transactional
    public AiRoadmapDto.ItemResponse completeItem(String userId, Long roadmapItemId) {
        AiRoadmap roadmap = findRoadmap(userId);
        AiRoadmapItem item = findOwnedItem(roadmap, roadmapItemId);
        item.complete(false);
        return AiRoadmapDto.ItemResponse.from(item);
    }

    private AiRoadmapItem.Status resolveStatus(AiRoadmapStandardItem standard, String corpus) {
        double ratio = matchRatio(standard, corpus);
        if (ratio >= 0.5) return AiRoadmapItem.Status.COMPLETED;
        if (ratio > 0) return AiRoadmapItem.Status.NEEDS_IMPROVEMENT;
        return AiRoadmapItem.Status.PENDING;
    }

    private double matchRatio(AiRoadmapStandardItem standard, String corpus) {
        if (corpus == null || corpus.isBlank()) return 0;
        Set<String> tokens = tokenize(standard.getTitle() + " " + Optional.ofNullable(standard.getKeyword()).orElse(""));
        if (tokens.isEmpty()) return 0;
        long matched = tokens.stream().filter(corpus::contains).count();
        return (double) matched / tokens.size();
    }

    private Set<String> tokenize(String value) {
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("[\\s,;/|·]+"))
                .map(String::trim)
                .filter(token -> token.length() >= 2)
                .collect(Collectors.toSet());
    }

    AiRoadmap findRoadmap(String userId) {
        return aiRoadmapRepository.findByMember_UserId(userId)
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.AI_ROADMAP_NOT_FOUND));
    }

    AiRoadmapItem findOwnedItem(AiRoadmap roadmap, Long roadmapItemId) {
        AiRoadmapItem item = aiRoadmapItemRepository.findById(roadmapItemId)
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.AI_ROADMAP_ITEM_NOT_FOUND));
        if (!Objects.equals(item.getAiRoadmap().getAiRoadmapId(), roadmap.getAiRoadmapId())) {
            throw new BaseException(AiRoadmapResponseCode.AI_ROADMAP_ITEM_NOT_FOUND);
        }
        return item;
    }

    Job findJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.JOB_NOT_FOUND));
    }

    void requireEligible(String userId) {
        AiRoadmapDto.EligibilityResponse eligibility = profileAnalyzer.checkEligibility(userId);
        if (eligibility.available()) return;
        if ("GRADE_RESTRICTED".equals(eligibility.reasonCode())) {
            throw new BaseException(AiRoadmapResponseCode.GRADE_RESTRICTED);
        }
        if ("TRACK_REQUIRED".equals(eligibility.reasonCode())) {
            throw new BaseException(AiRoadmapResponseCode.TRACK_REQUIRED);
        }
        throw new BaseException(AiRoadmapResponseCode.PROFILE_INCOMPLETE);
    }

    AiRoadmapDto.RoadmapResponse toRoadmapResponse(Member member, AiRoadmap roadmap, Integer grade,
                                                    AiRoadmapStandardItem.Category category) {
        List<AiRoadmapDto.ItemResponse> items = aiRoadmapItemRepository
                .findAllByAiRoadmap_AiRoadmapIdOrderByStandardItem_TargetGradeAscStandardItem_DisplayOrderAsc(roadmap.getAiRoadmapId())
                .stream()
                .filter(item -> grade == null || Objects.equals(item.getStandardItem().getTargetGrade(), grade))
                .filter(item -> category == null || item.getStandardItem().getCategory() == category)
                .filter(item -> item.getStatus() != AiRoadmapItem.Status.HIDDEN)
                .map(AiRoadmapDto.ItemResponse::from)
                .toList();
        return new AiRoadmapDto.RoadmapResponse(
                roadmap.getAiRoadmapId(), member.getUserId(), member.getName(), member.getGrade(), member.getGpa(),
                roadmap.getInterestJob().getJobId(), roadmap.getInterestJob().getJobName(), items
        );
    }

    @Transactional
    public AiRoadmapDto.StandardItemResponse createStandardItem(AiRoadmapDto.StandardItemRequest request) {
        Job job = findJob(request.jobId());
        AiRoadmapStandardItem item = AiRoadmapStandardItem.create(job, request.category(), request.targetGrade(),
                request.priority(), request.displayOrder(), request.title(), request.description(), request.keyword(),
                request.recommendationReason(), request.externalUrl(), request.requiredItem());
        return AiRoadmapDto.StandardItemResponse.from(standardItemRepository.save(item));
    }

    @Transactional
    public AiRoadmapDto.StandardItemResponse updateStandardItem(Long standardItemId,
                                                                 AiRoadmapDto.StandardItemRequest request) {
        AiRoadmapStandardItem item = standardItemRepository.findById(standardItemId)
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.STANDARD_ITEM_NOT_FOUND));
        if (!Objects.equals(item.getJob().getJobId(), request.jobId())) {
            throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }
        item.update(request.category(), request.targetGrade(), request.priority(), request.displayOrder(), request.title(),
                request.description(), request.keyword(), request.recommendationReason(), request.externalUrl(), request.requiredItem());
        return AiRoadmapDto.StandardItemResponse.from(item);
    }

    @Transactional
    public void deleteStandardItem(Long standardItemId) {
        if (!standardItemRepository.existsById(standardItemId)) {
            throw new BaseException(AiRoadmapResponseCode.STANDARD_ITEM_NOT_FOUND);
        }
        standardItemRepository.deleteById(standardItemId);
    }

    public List<AiRoadmapDto.StandardItemResponse> getStandardItems(Long jobId) {
        findJob(jobId);
        return standardItemRepository.findAllByJob_JobIdOrderByTargetGradeAscDisplayOrderAsc(jobId).stream()
                .map(AiRoadmapDto.StandardItemResponse::from)
                .toList();
    }

    List<AiRoadmapItem> getAllItems(AiRoadmap roadmap) {
        return aiRoadmapItemRepository
                .findAllByAiRoadmap_AiRoadmapIdOrderByStandardItem_TargetGradeAscStandardItem_DisplayOrderAsc(roadmap.getAiRoadmapId());
    }

    AiRoadmapStandardItem findStandardItem(Long id) {
        return standardItemRepository.findById(id)
                .orElseThrow(() -> new BaseException(AiRoadmapResponseCode.STANDARD_ITEM_NOT_FOUND));
    }

    AiRoadmapItemRepository itemRepository() {
        return aiRoadmapItemRepository;
    }

    AiRoadmapStandardItemRepository standardRepository() {
        return standardItemRepository;
    }
}
