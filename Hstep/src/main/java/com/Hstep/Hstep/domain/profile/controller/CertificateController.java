package com.Hstep.Hstep.domain.profile.controller;

import com.Hstep.Hstep.domain.profile.dto.CertificateDto;
import com.Hstep.Hstep.domain.profile.service.CertificateService;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/profile/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestBody CertificateDto.Request request
    ) {
        return ResponseEntity.ok(certificateService.create(principal.getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<CertificateDto.Response>> findAll(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ResponseEntity.ok(certificateService.findAllByUser(principal.getUserId()));
    }

    @PutMapping("/{certificateId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long certificateId,
            @RequestBody CertificateDto.Request request
    ) {
        certificateService.update(principal.getUserId(), certificateId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{certificateId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long certificateId
    ) {
        certificateService.delete(principal.getUserId(), certificateId);
        return ResponseEntity.ok().build();
    }
}