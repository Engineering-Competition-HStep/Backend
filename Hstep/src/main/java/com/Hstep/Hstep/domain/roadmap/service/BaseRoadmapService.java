package com.Hstep.Hstep.domain.roadmap.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.entity.MemberTrack;
import com.Hstep.Hstep.domain.member.exception.MemberResponseCode;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.roadmap.dto.BaseRoadmapDto;
import com.Hstep.Hstep.domain.roadmap.dto.BaseRoadmapItemDto;
import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmap;
import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmapItem;
import com.Hstep.Hstep.domain.roadmap.exception.RoadmapResponseCode;
import com.Hstep.Hstep.domain.roadmap.repository.BaseRoadmapItemRepository;
import com.Hstep.Hstep.domain.roadmap.repository.BaseRoadmapRepository;
import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BaseRoadmapService {

    private final BaseRoadmapRepository baseRoadmapRepository;
    private final BaseRoadmapItemRepository baseRoadmapItemRepository;
    private final TrackRepository trackRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(BaseRoadmapDto.Request request) {
        if (baseRoadmapRepository.existsByTrack_TrackId(request.trackId())) {
            throw new BaseException(RoadmapResponseCode.DUPLICATE_ROADMAP_FOR_TRACK);
        }
        Track track = trackRepository.findById(request.trackId())
                .orElseThrow(() -> new BaseException(RoadmapResponseCode.TRACK_NOT_FOUND));

        BaseRoadmap baseRoadmap = new BaseRoadmap(request.title(), track);
        return baseRoadmapRepository.save(baseRoadmap).getRoadmapId();
    }

    @Transactional
    public void update(Long roadmapId, BaseRoadmapDto.UpdateRequest request) {
        BaseRoadmap baseRoadmap = baseRoadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new BaseException(RoadmapResponseCode.ROADMAP_NOT_FOUND));
        baseRoadmap.update(request.title());
    }

    @Transactional
    public void delete(Long roadmapId) {
        if (!baseRoadmapRepository.existsById(roadmapId)) {
            throw new BaseException(RoadmapResponseCode.ROADMAP_NOT_FOUND);
        }
        baseRoadmapRepository.deleteById(roadmapId);
    }

    public BaseRoadmapDto.Response findByTrackId(Long trackId) {
        BaseRoadmap baseRoadmap = baseRoadmapRepository.findByTrack_TrackId(trackId)
                .orElseThrow(() -> new BaseException(RoadmapResponseCode.ROADMAP_NOT_FOUND));
        return BaseRoadmapDto.Response.from(baseRoadmap);
    }

    public List<BaseRoadmapDto.Response> findMyRoadmaps(String userId) {
        Member member = memberRepository.findWithTracksByUserId(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));

        List<Long> trackIds = member.getMemberTracks().stream()
                .sorted(Comparator.comparing(MemberTrack::getTrackOrder))
                .map(MemberTrack::getTrackId)
                .toList();

        return baseRoadmapRepository.findByTrack_TrackIdIn(trackIds).stream()
                .map(BaseRoadmapDto.Response::from)
                .toList();
    }

    @Transactional
    public Long addItem(Long roadmapId, BaseRoadmapItemDto.Request request) {
        BaseRoadmap baseRoadmap = baseRoadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new BaseException(RoadmapResponseCode.ROADMAP_NOT_FOUND));
        BaseRoadmapItem item = new BaseRoadmapItem(
                request.itemOrder(), request.grade(), request.semester(),
                request.category(), request.level(), request.title(), request.description(), baseRoadmap);
        return baseRoadmapItemRepository.save(item).getItemId();
    }

    @Transactional
    public void updateItem(Long itemId, BaseRoadmapItemDto.Request request) {
        BaseRoadmapItem item = baseRoadmapItemRepository.findById(itemId)
                .orElseThrow(() -> new BaseException(RoadmapResponseCode.ROADMAP_ITEM_NOT_FOUND));
        item.update(request.itemOrder(), request.grade(), request.semester(),
                request.category(), request.level(), request.title(), request.description());
    }

    @Transactional
    public void deleteItem(Long itemId) {
        if (!baseRoadmapItemRepository.existsById(itemId)) {
            throw new BaseException(RoadmapResponseCode.ROADMAP_ITEM_NOT_FOUND);
        }
        baseRoadmapItemRepository.deleteById(itemId);
    }
}