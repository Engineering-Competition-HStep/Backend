package com.Hstep.Hstep.domain.airoadmap.initializer;

import com.Hstep.Hstep.domain.airoadmap.entity.AiRoadmapItem;
import com.Hstep.Hstep.domain.airoadmap.repository.AiRoadmapItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AiRoadmapItemSnapshotBackfillInitializer {

    private final AiRoadmapItemRepository itemRepository;

    @Order(400)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        for (AiRoadmapItem item : itemRepository.findAll()) {
            item.backfillSnapshotIfMissing();
        }
    }
}
