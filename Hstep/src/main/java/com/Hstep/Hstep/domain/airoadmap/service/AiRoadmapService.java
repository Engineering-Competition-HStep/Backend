package com.Hstep.Hstep.domain.airoadmap.service;

import com.Hstep.Hstep.domain.airoadmap.dto.AiRoadmapDto;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmap;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapItem;
import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapStandardItem;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapItemSourceType;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapLane;
import com.Hstep.Hstep.domain.airoadmap.entity.RoadmapStage;
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
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
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
    private final RoadmapEvidenceMatcher evidenceMatcher;
    private final JobRecommendationProfileCatalog recommendationProfileCatalog;
    private final TrackRepository trackRepository;

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

        List<AiRoadmapDto.JobRecommendationResponse> scored = candidates.values().stream()
                .map(job -> scoreJob(member, memberTrackIds, corpus, job))
                .sorted(Comparator.comparingInt(AiRoadmapDto.JobRecommendationResponse::score).reversed()
                        .thenComparing(AiRoadmapDto.JobRecommendationResponse::jobName)
                        .thenComparing(AiRoadmapDto.JobRecommendationResponse::jobId))
                .toList();
        List<AiRoadmapDto.JobRecommendationResponse> selected = new ArrayList<>(3);
        Set<String> families = new HashSet<>();
        for (AiRoadmapDto.JobRecommendationResponse candidate : scored) {
            String family = recommendationProfileCatalog.familyOf(candidate.jobName());
            if (families.add(family)) selected.add(candidate);
            if (selected.size() == 3) return selected;
        }
        for (AiRoadmapDto.JobRecommendationResponse candidate : scored) {
            if (!selected.contains(candidate)) selected.add(candidate);
            if (selected.size() == 3) break;
        }
        return selected;
    }

    private AiRoadmapDto.JobRecommendationResponse scoreJob(Member member, Set<Long> trackIds,
                                                              String corpus, Job job) {
        boolean trackMatched = jobTrackRepository.findAllByJob_JobIdOrderByTrack_TrackNameAsc(job.getJobId()).stream()
                .anyMatch(relation -> trackIds.contains(relation.getTrack().getTrackId()));
        int trackScore = trackMatched ? 40 : 0;

        JobRecommendationProfileCatalog.JobSkillProfile profile = recommendationProfileCatalog.resolve(job);
        int specMatched = (int) profile.skillKeywords().stream().filter(corpus::contains).count();
        int courseMatched = (int) profile.courseKeywords().stream().filter(corpus::contains).count();
        int specScore = proportionalScore(specMatched, profile.skillKeywords().size(), 30);
        int courseScore = proportionalScore(courseMatched, profile.courseKeywords().size(), 20);
        int gradeScore = member.getGrade() != null && member.getGrade() >= 2 ? 10 : 0;
        int total = trackScore + specScore + courseScore + gradeScore;

        List<String> reasons = new ArrayList<>();
        if (trackScore > 0) reasons.add("현재 소속 트랙과 직접 연결된 직무입니다.");
        if (specScore > 0) reasons.add("등록한 개인 스펙과 직무 역량 키워드의 연관성이 있습니다.");
        if (courseScore > 0) reasons.add("등록한 경험이 직무 기초 역량과 연관됩니다.");
        if (gradeScore > 0) reasons.add("현재 학년이 개인 맞춤 로드맵 이용 조건을 충족합니다.");
        if (reasons.isEmpty()) reasons.add("현재 등록된 트랙 기준으로 추천된 직무입니다.");

        return new AiRoadmapDto.JobRecommendationResponse(job.getJobId(), job.getJobName(), total,
                trackScore, specScore, courseScore, gradeScore, reasons);
    }

    private int proportionalScore(int matched, int total, int maxScore) {
        if (total == 0 || matched == 0) return 0;
        return Math.min(maxScore, (int) Math.round((double) matched / total * maxScore));
    }

    @Transactional
    public AiRoadmapDto.RoadmapResponse createInitialRoadmap(String userId, Long jobId) {
        requireEligible(userId);
        if (aiRoadmapRepository.existsByMember_UserId(userId)) {
            throw new BaseException(AiRoadmapResponseCode.AI_ROADMAP_ALREADY_EXISTS);
        }
        validateRecommendedJob(userId, jobId);

        Member member = profileAnalyzer.getMemberWithTracks(userId);
        Job job = findJob(jobId);
        List<AiRoadmapStandardItem> standards = getStandardsOrThrow(jobId);
        AiRoadmap roadmap = aiRoadmapRepository.save(AiRoadmap.create(member, job));
        createRequiredItems(userId, roadmap, standards);
        return toRoadmapResponse(member, roadmap, null, null);
    }

    @Transactional
    AiRoadmapDto.RoadmapResponse replaceInterestJob(String userId, Long jobId) {
        requireEligible(userId);
        validateRecommendedJob(userId, jobId);

        Member member = profileAnalyzer.getMemberWithTracks(userId);
        Job job = findJob(jobId);
        List<AiRoadmapStandardItem> standards = getStandardsOrThrow(jobId);
        AiRoadmap roadmap = findRoadmap(userId);

        if (Objects.equals(roadmap.getInterestJob().getJobId(), jobId)) {
            throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }

        List<AiRoadmapItem> templateItems = getAllItems(roadmap).stream()
                .filter(item -> item.getSourceType() == RoadmapItemSourceType.STANDARD_TEMPLATE)
                .toList();
        aiRoadmapItemRepository.deleteAll(templateItems);
        aiRoadmapItemRepository.flush();
        roadmap.changeInterestJob(job);
        createRequiredItems(userId, roadmap, standards);
        return toRoadmapResponse(member, roadmap, null, null);
    }

    private void validateRecommendedJob(String userId, Long jobId) {
        boolean recommended = recommendJobs(userId).stream()
                .anyMatch(job -> Objects.equals(job.jobId(), jobId));
        if (!recommended) {
            throw new BaseException(AiRoadmapResponseCode.UNSUPPORTED_ACTION);
        }
    }

    private List<AiRoadmapStandardItem> getStandardsOrThrow(Long jobId) {
        List<AiRoadmapStandardItem> standards = standardItemRepository
                .findAllByJob_JobIdOrderByTargetGradeAscDisplayOrderAsc(jobId);
        if (standards.isEmpty()) {
            throw new BaseException(AiRoadmapResponseCode.STANDARD_ROADMAP_NOT_FOUND);
        }
        return standards;
    }

    private void createRequiredItems(String userId, AiRoadmap roadmap, List<AiRoadmapStandardItem> standards) {
        List<AiRoadmapItem> items = standards.stream()
                .filter(AiRoadmapStandardItem::isDefaultIncludedEffective)
                .map(standard -> AiRoadmapItem.fromStandard(
                        roadmap, standard, evidenceMatcher.resolve(userId, standard), false))
                .toList();
        aiRoadmapItemRepository.saveAll(items);
    }

    public AiRoadmapDto.RoadmapResponse getMyRoadmap(String userId, Integer grade,
                                                       AiRoadmapStandardItem.Category category) {
        return getMyRoadmap(userId, grade, category, null, null);
    }

    public AiRoadmapDto.RoadmapResponse getMyRoadmap(String userId, Integer grade,
                                                      AiRoadmapStandardItem.Category category,
                                                      RoadmapStage stage, RoadmapLane lane) {
        requireEligible(userId);
        Member member = profileAnalyzer.getMemberWithTracks(userId);
        AiRoadmap roadmap = findRoadmap(userId);
        return toRoadmapResponse(member, roadmap, grade, category, stage, lane);
    }

    @Transactional
    public AiRoadmapDto.ItemResponse completeItem(String userId, Long roadmapItemId) {
        AiRoadmap roadmap = findRoadmap(userId);
        AiRoadmapItem item = findOwnedItem(roadmap, roadmapItemId);
        item.complete(false);
        return AiRoadmapDto.ItemResponse.from(item);
    }

    @Transactional
    public AiRoadmapDto.ItemResponse reopenItem(String userId, Long roadmapItemId) {
        AiRoadmap roadmap = findRoadmap(userId);
        AiRoadmapItem item = findOwnedItem(roadmap, roadmapItemId);
        item.reopen();
        return AiRoadmapDto.ItemResponse.from(item);
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
        return toRoadmapResponse(member, roadmap, grade, category, null, null);
    }

    AiRoadmapDto.RoadmapResponse toRoadmapResponse(Member member, AiRoadmap roadmap, Integer grade,
                                                    AiRoadmapStandardItem.Category category,
                                                    RoadmapStage stage, RoadmapLane lane) {
        List<AiRoadmapDto.ItemResponse> items = aiRoadmapItemRepository
                .findAllByAiRoadmap_AiRoadmapId(roadmap.getAiRoadmapId())
                .stream()
                .peek(AiRoadmapItem::backfillSnapshotIfMissing)
                .filter(item -> grade == null || Objects.equals(item.getTargetGrade(), grade))
                .filter(item -> category == null || item.getCategory() == category)
                .filter(item -> stage == null || item.getTargetStage() == stage)
                .filter(item -> lane == null || item.getRoadmapLane() == lane)
                .filter(item -> item.getStatus() != AiRoadmapItem.Status.HIDDEN)
                .sorted(Comparator.comparing(AiRoadmapItem::getTargetGrade)
                        .thenComparing(AiRoadmapItem::getDisplayOrder)
                        .thenComparing(AiRoadmapItem::getAiRoadmapItemId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .map(AiRoadmapDto.ItemResponse::from)
                .toList();
        AiRoadmapDto.RoadmapSummary summary = summarize(items);
        return new AiRoadmapDto.RoadmapResponse(
                roadmap.getAiRoadmapId(), member.getUserId(), member.getName(), member.getGrade(), member.getGpa(),
                roadmap.getInterestJob().getJobId(), roadmap.getInterestJob().getJobName(), items, summary
        );
    }

    private AiRoadmapDto.RoadmapSummary summarize(List<AiRoadmapDto.ItemResponse> items) {
        int completed = (int) items.stream().filter(item -> item.status() == AiRoadmapItem.Status.COMPLETED).count();
        int needsImprovement = (int) items.stream()
                .filter(item -> item.status() == AiRoadmapItem.Status.NEEDS_IMPROVEMENT).count();
        int pending = (int) items.stream().filter(item -> item.status() == AiRoadmapItem.Status.PENDING).count();
        int total = items.size();
        int progressRate = total == 0 ? 0 : (int) Math.round((double) completed / total * 100);
        Map<RoadmapLane, Long> laneCounts = Arrays.stream(RoadmapLane.values())
                .collect(Collectors.toMap(lane -> lane,
                        lane -> items.stream().filter(item -> item.roadmapLane() == lane).count(),
                        (left, right) -> left, LinkedHashMap::new));
        return new AiRoadmapDto.RoadmapSummary(total, completed, needsImprovement, pending,
                progressRate, laneCounts);
    }

    public AiRoadmapDto.EntryResponse getEntry(String userId) {
        AiRoadmapDto.EligibilityResponse eligibility = profileAnalyzer.checkEligibility(userId);
        Member member = profileAnalyzer.getMemberWithTracks(userId);
        List<AiRoadmapDto.MemberTrackResponse> tracks = member.getMemberTracks().stream()
                .map(memberTrack -> trackRepository.findById(memberTrack.getTrackId())
                        .map(track -> new AiRoadmapDto.MemberTrackResponse(track.getTrackId(), track.getTrackName()))
                        .orElse(new AiRoadmapDto.MemberTrackResponse(memberTrack.getTrackId(), null)))
                .toList();
        if (!eligibility.available()) {
            AiRoadmapDto.EntryState state = "GRADE_RESTRICTED".equals(eligibility.reasonCode())
                    ? AiRoadmapDto.EntryState.GRADE_RESTRICTED : AiRoadmapDto.EntryState.PROFILE_REQUIRED;
            return new AiRoadmapDto.EntryResponse(state, eligibility.reasonCode(), eligibility.message(),
                    eligibility.moveToMyPage(), tracks, List.of(), null);
        }
        Optional<AiRoadmap> roadmap = aiRoadmapRepository.findByMember_UserId(userId);
        if (roadmap.isEmpty()) {
            return new AiRoadmapDto.EntryResponse(AiRoadmapDto.EntryState.JOB_SELECTION_REQUIRED, null,
                    "추천 직무를 선택하면 개인 맞춤 로드맵이 생성됩니다.", false,
                    tracks, recommendJobs(userId), null);
        }
        return new AiRoadmapDto.EntryResponse(AiRoadmapDto.EntryState.ROADMAP_READY, null,
                "개인 맞춤 로드맵을 조회했습니다.", false, tracks, List.of(),
                toRoadmapResponse(member, roadmap.get(), null, null));
    }

    @Transactional
    public AiRoadmapDto.StandardItemResponse createStandardItem(AiRoadmapDto.StandardItemRequest request) {
        Job job = findJob(request.jobId());
        AiRoadmapStandardItem item = AiRoadmapStandardItem.create(job, request.category(), request.targetGrade(),
                request.priority(), request.displayOrder(), request.title(), request.description(), request.keyword(),
                request.recommendationReason(), request.externalUrl(), request.requiredItem());
        item.update(request.category(), request.targetGrade(), request.priority(), request.displayOrder(), request.title(),
                request.description(), request.keyword(), request.recommendationReason(), request.externalUrl(),
                request.requiredItem(), request.roadmapLane(), request.itemType(), request.targetStage(),
                request.coreItem(), request.defaultIncluded());
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
                request.description(), request.keyword(), request.recommendationReason(), request.externalUrl(),
                request.requiredItem(), request.roadmapLane(), request.itemType(), request.targetStage(),
                request.coreItem(), request.defaultIncluded());
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
                .findAllByAiRoadmap_AiRoadmapId(roadmap.getAiRoadmapId()).stream()
                .peek(AiRoadmapItem::backfillSnapshotIfMissing)
                .sorted(Comparator.comparing(AiRoadmapItem::getTargetGrade)
                        .thenComparing(AiRoadmapItem::getDisplayOrder))
                .toList();
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
