package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.VolunteerDto;
import com.Hstep.Hstep.domain.profile.service.VolunteerService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody VolunteerDto.Request request
    ) {
        return ResponseEntity.ok(volunteerService.create(principal.getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<VolunteerDto.Response>> findAll(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ResponseEntity.ok(volunteerService.findAllByUser(principal.getUserId()));
    }

    @PutMapping("/{volunteerId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long volunteerId,
            @RequestBody VolunteerDto.Request request
    ) {
        volunteerService.update(principal.getUserId(), volunteerId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{volunteerId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long volunteerId
    ) {
        volunteerService.delete(principal.getUserId(), volunteerId);
        return ResponseEntity.ok().build();
    }
}