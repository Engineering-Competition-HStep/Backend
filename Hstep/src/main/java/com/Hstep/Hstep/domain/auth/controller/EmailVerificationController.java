package com.Hstep.Hstep.domain.auth.controller;

import com.Hstep.Hstep.domain.auth.dto.EmailVerificationDto;
import com.Hstep.Hstep.domain.auth.exception.AuthResponseCode;
import com.Hstep.Hstep.domain.auth.service.EmailVerificationService;
import com.Hstep.Hstep.global.response.SuccessResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email-verifications")
@RequiredArgsConstructor
@Validated
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping
    public SuccessResponse<Void> sendVerification(
            @Valid @RequestBody EmailVerificationDto.SendRequest request
    ) {
        emailVerificationService.sendVerification(request.email());
        return SuccessResponse.empty(AuthResponseCode.EMAIL_VERIFICATION_SENT);
    }

    @GetMapping("/verify")
    public SuccessResponse<Void> verify(
            @RequestParam @NotBlank String token
    ) {
        emailVerificationService.verify(token);
        return SuccessResponse.empty(AuthResponseCode.EMAIL_VERIFICATION_SUCCESS);
    }
}
