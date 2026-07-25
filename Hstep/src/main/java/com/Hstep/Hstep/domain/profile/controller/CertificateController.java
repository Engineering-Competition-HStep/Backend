package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.CertificateDto;
import com.Hstep.Hstep.domain.profile.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    // userId는 인증(JWT) 붙으면 파라미터 대신 로그인 정보에서 추출
    @PostMapping
    public ResponseEntity<Long> create(@RequestParam String userId, @RequestBody CertificateDto.Request request) {
        return ResponseEntity.ok(certificateService.create(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<CertificateDto.Response>> findAll(@RequestParam String userId) {
        return ResponseEntity.ok(certificateService.findAllByUser(userId));
    }

    @PutMapping("/{certificateId}")
    public ResponseEntity<Void> update(@PathVariable Long certificateId, @RequestBody CertificateDto.Request request) {
        certificateService.update(certificateId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{certificateId}")
    public ResponseEntity<Void> delete(@PathVariable Long certificateId) {
        certificateService.delete(certificateId);
        return ResponseEntity.ok().build();
    }
}