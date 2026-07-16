package com.Hstep.Hstep.domain.member.controller;

import com.Hstep.Hstep.domain.member.dto.MemberDto.MemberRes;
import com.Hstep.Hstep.domain.member.dto.MemberDto.UpdateReq;
import com.Hstep.Hstep.domain.member.exception.MemberResponseCode;
import com.Hstep.Hstep.domain.member.service.MemberService;
import com.Hstep.Hstep.global.response.SuccessResponse;
import com.Hstep.Hstep.global.security.MemberPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public SuccessResponse<MemberRes> getMe(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return SuccessResponse.of(
                memberService.getMe(principal.getUserId()),
                MemberResponseCode.MEMBER_GET_SUCCESS
        );
    }

    @PatchMapping("/me")
    public SuccessResponse<MemberRes> updateMe(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody UpdateReq updateReq
    ) {
        return SuccessResponse.of(
                memberService.updateMe(principal.getUserId(), updateReq),
                MemberResponseCode.MEMBER_UPDATE_SUCCESS
        );
    }

    @DeleteMapping("/me")
    public SuccessResponse<Void> deleteMe(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        memberService.deleteMe(principal.getUserId());
        return SuccessResponse.empty(MemberResponseCode.MEMBER_DELETE_SUCCESS);
    }
}
