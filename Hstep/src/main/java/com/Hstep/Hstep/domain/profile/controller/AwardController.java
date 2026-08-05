package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.AwardDto;
import com.Hstep.Hstep.domain.profile.service.AwardService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/awards")
@RequiredArgsConstructor
public class AwardController {

    private final AwardService awardService;

    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody AwardDto.Request request
    ) {
        return ResponseEntity.ok(awardService.create(principal.getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<AwardDto.Response>> findAll(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ResponseEntity.ok(awardService.findAllByUser(principal.getUserId()));
    }

    @PutMapping("/{awardId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long awardId,
            @RequestBody AwardDto.Request request
    ) {
        awardService.update(principal.getUserId(), awardId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{awardId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long awardId
    ) {
        awardService.delete(principal.getUserId(), awardId);
        return ResponseEntity.ok().build();
    }
}