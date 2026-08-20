package com.Hstep.Hstep.domain.auth.controller;

import com.Hstep.Hstep.domain.auth.dto.EmailVerificationDto;
import com.Hstep.Hstep.domain.auth.exception.AuthResponseCode;
import com.Hstep.Hstep.domain.auth.service.EmailVerificationService;
import com.Hstep.Hstep.global.response.SuccessResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/verify", produces = MediaType.TEXT_HTML_VALUE)
    public String verify(
            @RequestParam @NotBlank String token
    ) {
        emailVerificationService.verify(token);
        return """
                <!doctype html>
                <html lang="ko">
                <head><meta charset="utf-8"><title>HSTEP 이메일 인증</title></head>
                <body style="font-family:Arial,sans-serif;text-align:center;padding:72px 20px;color:#222;">
                  <h2>이메일 인증이 완료되었습니다.</h2>
                  <p>HSTEP 회원가입 화면으로 돌아가 회원가입을 완료해주세요.</p>
                </body>
                </html>
                """;
    }
}
