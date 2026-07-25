package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.AwardDto;
import com.Hstep.Hstep.domain.profile.service.AwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/awards")
@RequiredArgsConstructor
public class AwardController {

    private final AwardService awardService;

    @PostMapping
    public ResponseEntity<Long> create(@RequestParam String userId, @RequestBody AwardDto.Request request) {
        return ResponseEntity.ok(awardService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<AwardDto.Response>> findAll(@RequestParam String userId) {
        return ResponseEntity.ok(awardService.findAllByUser(userId));
    }

    @PutMapping("/{awardId}")
    public ResponseEntity<Void> update(@PathVariable Long awardId, @RequestBody AwardDto.Request request) {
        awardService.update(awardId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{awardId}")
    public ResponseEntity<Void> delete(@PathVariable Long awardId) {
        awardService.delete(awardId);
        return ResponseEntity.ok().build();
    }
}