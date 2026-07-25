package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.VolunteerDto;
import com.Hstep.Hstep.domain.profile.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @PostMapping
    public ResponseEntity<Long> create(@RequestParam String userId, @RequestBody VolunteerDto.Request request) {
        return ResponseEntity.ok(volunteerService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<VolunteerDto.Response>> findAll(@RequestParam String userId) {
        return ResponseEntity.ok(volunteerService.findAllByUser(userId));
    }

    @PutMapping("/{volunteerId}")
    public ResponseEntity<Void> update(@PathVariable Long volunteerId, @RequestBody VolunteerDto.Request request) {
        volunteerService.update(volunteerId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{volunteerId}")
    public ResponseEntity<Void> delete(@PathVariable Long volunteerId) {
        volunteerService.delete(volunteerId);
        return ResponseEntity.ok().build();
    }
}