package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.UserGradeGpaDto;
import com.Hstep.Hstep.domain.profile.service.UserGradeGpaService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/grade-gpa")
@RequiredArgsConstructor
public class UserGradeGpaController {

    private final UserGradeGpaService userGradeGpaService;

    @PutMapping
    public ResponseEntity<Long> save(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody UserGradeGpaDto.Request request
    ) {
        return ResponseEntity.ok(userGradeGpaService.save(principal.getUserId(), request));
    }

    // '저장하기' 버튼
    @PutMapping("/bulk")
    public ResponseEntity<Void> saveAll(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody List<UserGradeGpaDto.Request> requests
    ) {
        userGradeGpaService.saveAll(principal.getUserId(), requests);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserGradeGpaDto.Response>> findAll(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ResponseEntity.ok(userGradeGpaService.findAllByUser(principal.getUserId()));
    }

    @DeleteMapping("/{userGradeGpaId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long userGradeGpaId
    ) {
        userGradeGpaService.delete(principal.getUserId(), userGradeGpaId);
        return ResponseEntity.ok().build();
    }
}