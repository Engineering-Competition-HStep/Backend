package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.UserGradeGpaDto;
import com.Hstep.Hstep.domain.profile.service.UserGradeGpaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/grade-gpa")
@RequiredArgsConstructor
public class UserGradeGpaController {

    private final UserGradeGpaService userGradeGpaService;

    @PutMapping
    public ResponseEntity<Long> save(@RequestParam String userId, @RequestBody UserGradeGpaDto.Request request) {
        return ResponseEntity.ok(userGradeGpaService.save(userId, request));
    }

    // '저장하기' 버튼
    @PutMapping("/bulk")
    public ResponseEntity<Void> saveAll(@RequestParam String userId, @RequestBody List<UserGradeGpaDto.Request> requests) {
        userGradeGpaService.saveAll(userId, requests);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserGradeGpaDto.Response>> findAll(@RequestParam String userId) {
        return ResponseEntity.ok(userGradeGpaService.findAllByUser(userId));
    }

    @DeleteMapping("/{userGradeGpaId}")
    public ResponseEntity<Void> delete(@PathVariable Long userGradeGpaId) {
        userGradeGpaService.delete(userGradeGpaId);
        return ResponseEntity.ok().build();
    }
}