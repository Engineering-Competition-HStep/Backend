package com.Hstep.Hstep.domain.roadmap.initializer;

import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmap;
import com.Hstep.Hstep.domain.roadmap.entity.BaseRoadmapItem;
import com.Hstep.Hstep.domain.roadmap.initializer.RoadmapSeedCatalog.ItemSeed;
import com.Hstep.Hstep.domain.roadmap.initializer.RoadmapSeedCatalog.TrackRoadmapSeed;
import com.Hstep.Hstep.domain.roadmap.repository.BaseRoadmapRepository;
import com.Hstep.Hstep.domain.track.entity.Track;
import com.Hstep.Hstep.domain.track.repository.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RoadmapDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(RoadmapDataInitializer.class);

    private final TrackRepository trackRepository;
    private final BaseRoadmapRepository baseRoadmapRepository;

    public RoadmapDataInitializer(
            TrackRepository trackRepository,
            BaseRoadmapRepository baseRoadmapRepository
    ) {
        this.trackRepository = trackRepository;
        this.baseRoadmapRepository = baseRoadmapRepository;
    }

    /**
     * TRACK 초기화가 끝난 뒤 트랙별 기본 로드맵을 채웁니다.
     * 이미 로드맵이 있는 트랙은 건드리지 않고, 없는 트랙만 시딩합니다.
     */
    @Order(210)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initialize() {
        List<Track> tracks = trackRepository.findAll();

        if (tracks.isEmpty()) {
            log.warn("TRACK 데이터가 없어 BaseRoadmap 초기화를 수행하지 않았습니다.");
            return;
        }

        Map<String, Track> trackByNormalizedName = tracks.stream()
                .collect(Collectors.toMap(
                        track -> normalizeTrackName(track.getTrackName()),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        List<String> unmatchedCatalogTracks = new ArrayList<>();
        List<BaseRoadmap> newRoadmaps = new ArrayList<>();
        int alreadySeededCount = 0;

        for (TrackRoadmapSeed seed : RoadmapSeedCatalog.all()) {
            Track track = trackByNormalizedName.get(normalizeTrackName(seed.trackName()));

            if (track == null) {
                unmatchedCatalogTracks.add(seed.trackName());
                continue;
            }

            if (baseRoadmapRepository.existsByTrack_TrackId(track.getTrackId())) {
                alreadySeededCount++;
                continue;
            }

            newRoadmaps.add(createRoadmap(track, seed.items()));
        }

        if (!newRoadmaps.isEmpty()) {
            baseRoadmapRepository.saveAll(newRoadmaps);
        }

        log.info(
                "BaseRoadmap 초기화 완료. createdRoadmaps={}, alreadySeeded={}, unmatchedTracks={}",
                newRoadmaps.size(),
                alreadySeededCount,
                unmatchedCatalogTracks.size()
        );

        if (!unmatchedCatalogTracks.isEmpty()) {
            log.info(
                    "DB에 존재하지 않아 시딩하지 않은 RoadmapSeedCatalog 트랙 수={}, tracks={}",
                    unmatchedCatalogTracks.size(),
                    unmatchedCatalogTracks
            );
        }
    }

    private BaseRoadmap createRoadmap(Track track, List<ItemSeed> itemSeeds) {
        BaseRoadmap roadmap = new BaseRoadmap(track.getTrackName() + " 기본 로드맵", track);

        int order = 1;
        for (ItemSeed itemSeed : itemSeeds) {
            roadmap.getItems().add(new BaseRoadmapItem(
                    order++,
                    itemSeed.grade(),
                    itemSeed.semester(),
                    itemSeed.category(),
                    itemSeed.level(),
                    itemSeed.title(),
                    null,
                    roadmap
            ));
        }

        return roadmap;
    }

    private String normalizeTrackName(String value) {
        if (value == null) {
            return "";
        }

        String stripped = value.replaceAll("[\\s·ㆍ・&/\\\\_\\-()]", "");
        String normalized = Normalizer.normalize(stripped, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);

        return normalized.replaceFirst("(트랙|전공|학과)$", "");
    }
}
