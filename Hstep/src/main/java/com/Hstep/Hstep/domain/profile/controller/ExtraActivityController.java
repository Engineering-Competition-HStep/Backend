package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.ExtraActivityDto;
import com.Hstep.Hstep.domain.profile.service.ExtraActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/activities")
@RequiredArgsConstructor
public class ExtraActivityController {

    private final ExtraActivityService extraActivityService;

    @PostMapping
    public ResponseEntity<Long> create(@RequestParam String userId, @RequestBody ExtraActivityDto.Request request) {
        return ResponseEntity.ok(extraActivityService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<ExtraActivityDto.Response>> findAll(@RequestParam String userId) {
        return ResponseEntity.ok(extraActivityService.findAllByUser(userId));
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<Void> update(@PathVariable Long activityId, @RequestBody ExtraActivityDto.Request request) {
        extraActivityService.update(activityId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> delete(@PathVariable Long activityId) {
        extraActivityService.delete(activityId);
        return ResponseEntity.ok().build();
    }
}