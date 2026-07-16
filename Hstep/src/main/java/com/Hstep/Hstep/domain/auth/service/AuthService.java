package com.Hstep.Hstep.domain.auth.service;

import com.Hstep.Hstep.domain.auth.dto.AuthDto.ChangePasswordReq;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.CheckAvailableRes;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.LoginReq;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.SignupReq;
import com.Hstep.Hstep.domain.auth.dto.AuthDto.TokenRes;
import com.Hstep.Hstep.domain.auth.exception.AuthResponseCode;
import com.Hstep.Hstep.domain.member.dto.MemberDto.MemberRes;
import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.exception.MemberResponseCode;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import com.Hstep.Hstep.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MemberRes signup(SignupReq signupReq) {
        if (memberRepository.existsById(signupReq.getUserId())) {
            throw new BaseException(AuthResponseCode.USER_ID_DUPLICATION);
        }

        String normalizedEmail = signupReq.getEmail().trim().toLowerCase(Locale.ROOT);
        if (memberRepository.existsByEmail(normalizedEmail)) {
            throw new BaseException(AuthResponseCode.EMAIL_DUPLICATION);
        }

        validateTracks(signupReq.getTrackIds());

        Member member = Member.create(
                signupReq.getUserId(),
                normalizedEmail,
                passwordEncoder.encode(signupReq.getPassword()),
                signupReq.getName().trim(),
                signupReq.getGrade()
        );
        member.replaceTracks(signupReq.getTrackIds());

        try {
            return MemberRes.fromEntity(memberRepository.save(member));
        } catch (DataIntegrityViolationException exception) {
            throw new BaseException(AuthResponseCode.USER_ID_DUPLICATION);
        }
    }

    @Transactional(readOnly = true)
    public TokenRes login(LoginReq loginReq) {
        Member member = memberRepository.findWithTracksByUserId(loginReq.getUserId())
                .orElseThrow(() -> new BaseException(AuthResponseCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginReq.getPassword(), member.getPassword())) {
            throw new BaseException(AuthResponseCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);
        return TokenRes.of(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                MemberRes.fromEntity(member)
        );
    }

    @Transactional(readOnly = true)
    public CheckAvailableRes checkUserId(Long userId) {
        if (memberRepository.existsById(userId)) {
            throw new BaseException(AuthResponseCode.USER_ID_DUPLICATION);
        }
        return new CheckAvailableRes(true);
    }

    @Transactional(readOnly = true)
    public CheckAvailableRes checkEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (memberRepository.existsByEmail(normalizedEmail)) {
            throw new BaseException(AuthResponseCode.EMAIL_DUPLICATION);
        }
        return new CheckAvailableRes(true);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordReq changePasswordReq) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BaseException(MemberResponseCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(changePasswordReq.getCurrentPassword(), member.getPassword())) {
            throw new BaseException(AuthResponseCode.CURRENT_PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(changePasswordReq.getNewPassword(), member.getPassword())) {
            throw new BaseException(AuthResponseCode.SAME_PASSWORD);
        }

        member.changePassword(passwordEncoder.encode(changePasswordReq.getNewPassword()));
    }

    private void validateTracks(List<Long> trackIds) {
        long distinctTrackCount = trackIds.stream().distinct().count();
        if (trackIds.isEmpty() || trackIds.size() > 2 || distinctTrackCount != trackIds.size()) {
            throw new BaseException(AuthResponseCode.INVALID_TRACKS);
        }
    }
}
