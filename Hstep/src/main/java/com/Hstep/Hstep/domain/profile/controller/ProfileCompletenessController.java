package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.ProfileCompletenessDto;
import com.Hstep.Hstep.domain.profile.service.ProfileCompletenessService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileCompletenessController {

    private final ProfileCompletenessService profileCompletenessService;

    @GetMapping("/completeness")
    public ResponseEntity<ProfileCompletenessDto.Response> checkCompleteness(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ResponseEntity.ok(profileCompletenessService.check(principal.getUserId()));
    }
}