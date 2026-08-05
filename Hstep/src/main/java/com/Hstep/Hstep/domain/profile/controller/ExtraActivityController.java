package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.ExtraActivityDto;
import com.Hstep.Hstep.domain.profile.service.ExtraActivityService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/activities")
@RequiredArgsConstructor
public class ExtraActivityController {

    private final ExtraActivityService extraActivityService;

    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody ExtraActivityDto.Request request
    ) {
        return ResponseEntity.ok(extraActivityService.create(principal.getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ExtraActivityDto.Response>> findAll(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ResponseEntity.ok(extraActivityService.findAllByUser(principal.getUserId()));
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long activityId,
            @RequestBody ExtraActivityDto.Request request
    ) {
        extraActivityService.update(principal.getUserId(), activityId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long activityId
    ) {
        extraActivityService.delete(principal.getUserId(), activityId);
        return ResponseEntity.ok().build();
    }
}