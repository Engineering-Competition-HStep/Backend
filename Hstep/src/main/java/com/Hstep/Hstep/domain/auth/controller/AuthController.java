package com.Hstep.Hstep.domain.auth.controller;

import com.Hstep.Hstep.domain.auth.dto.AuthDto.ChangePasswordReq;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.CheckAvailableRes;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.LoginReq;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.SignupReq;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.TokenRes;
import com.Hstep.Hstep.domain.auth.exception.AuthResponseCode;
import com.Hstep.Hstep.domain.auth.service.AuthService;
import com.Hstep.Hstep.domain.member.dto.MemberDto.MemberRes;
import com.Hstep.Hstep.global.response.SuccessResponse;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public SuccessResponse<MemberRes> signup(
            @Valid @RequestBody SignupReq signupReq
    ) {
        return SuccessResponse.of(authService.signup(signupReq), AuthResponseCode.SIGNUP_SUCCESS);
    }

    @PostMapping("/login")
    public SuccessResponse<TokenRes> login(
            @Valid @RequestBody LoginReq loginReq
    ) {
        return SuccessResponse.of(authService.login(loginReq), AuthResponseCode.LOGIN_SUCCESS);
    }

    @GetMapping("/check/student-number")
    public SuccessResponse<CheckAvailableRes> checkUserId(
            @RequestParam
            @Min(value = 1_000_000L, message = "학번 형식을 확인해주세요.")
            @Max(value = 9_999_999_999L, message = "학번 형식을 확인해주세요.")
            Long userId
    ) {
        return SuccessResponse.of(
                authService.checkUserId(userId),
                AuthResponseCode.USER_ID_AVAILABLE
        );
    }

    @GetMapping("/check/email")
    public SuccessResponse<CheckAvailableRes> checkEmail(
            @RequestParam
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Pattern(
                    regexp = "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)*hansung\\.ac\\.kr$",
                    message = "한성대학교 이메일만 사용할 수 있습니다."
            )
            String email
    ) {
        return SuccessResponse.of(authService.checkEmail(email), AuthResponseCode.EMAIL_AVAILABLE);
    }

    @PatchMapping("/password")
    public SuccessResponse<Void> changePassword(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody ChangePasswordReq changePasswordReq
    ) {
        authService.changePassword(principal.getUserId(), changePasswordReq);
        return SuccessResponse.empty(AuthResponseCode.PASSWORD_CHANGE_SUCCESS);
    }
}
