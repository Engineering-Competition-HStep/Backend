package com.Hstep.Hstep.domain.roadmap.controller;

import com.Hstep.Hstep.domain.roadmap.dto.BaseRoadmapDto;
import com.Hstep.Hstep.domain.roadmap.dto.BaseRoadmapItemDto;
import com.Hstep.Hstep.domain.roadmap.service.BaseRoadmapService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roadmaps/base")
public class BaseRoadmapController {

    private final BaseRoadmapService baseRoadmapService;

    @GetMapping
    public ResponseEntity<List<BaseRoadmapDto.Response>> findMyRoadmaps(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ResponseEntity.ok(baseRoadmapService.findMyRoadmaps(principal.getUserId()));
    }

    @GetMapping("/track/{trackId}")
    public ResponseEntity<BaseRoadmapDto.Response> findByTrackId(@PathVariable Long trackId) {
        return ResponseEntity.ok(baseRoadmapService.findByTrackId(trackId));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody BaseRoadmapDto.Request request) {
        Long roadmapId = baseRoadmapService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/roadmaps/base/" + roadmapId))
                .build();
    }

    @PutMapping("/{roadmapId}")
    public ResponseEntity<Void> update(
            @PathVariable Long roadmapId,
            @Valid @RequestBody BaseRoadmapDto.UpdateRequest request
    ) {
        baseRoadmapService.update(roadmapId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roadmapId}")
    public ResponseEntity<Void> delete(@PathVariable Long roadmapId) {
        baseRoadmapService.delete(roadmapId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roadmapId}/items")
    public ResponseEntity<Void> addItem(
            @PathVariable Long roadmapId,
            @Valid @RequestBody BaseRoadmapItemDto.Request request
    ) {
        Long itemId = baseRoadmapService.addItem(roadmapId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/roadmaps/base/items/" + itemId))
                .build();
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Void> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody BaseRoadmapItemDto.Request request
    ) {
        baseRoadmapService.updateItem(itemId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        baseRoadmapService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}