package com.Hstep.Hstep.domain.auth.service;

import com.Hstep.Hstep.domain.auth.entity.EmailVerification;
import com.Hstep.Hstep.domain.auth.exception.AuthResponseCode;
import com.Hstep.Hstep.domain.auth.repository.EmailVerificationRepository;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final JavaMailSender mailSender;

    @Value("${app.email-verification.from}")
    private String fromAddress;

    @Value("${app.email-verification.verify-base-url}")
    private String verifyBaseUrl;

    @Value("${app.email-verification.expiration-minutes:15}")
    private long expirationMinutes;

    @Transactional
    public void sendVerification(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        if (memberRepository.existsByEmail(email)) {
            throw new BaseException(AuthResponseCode.EMAIL_DUPLICATION);
        }

        String rawToken = createToken();
        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

        emailVerificationRepository.deleteByEmail(email);
        emailVerificationRepository.save(EmailVerification.create(email, tokenHash, expiresAt));

        String verificationUrl = trimTrailingSlash(verifyBaseUrl)
                + "/api/auth/email-verifications/verify?token=" + rawToken;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("[HSTEP] 한성대학교 이메일 인증");
            helper.setText(buildVerificationMail(verificationUrl), true);
            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException exception) {
            throw new BaseException(AuthResponseCode.EMAIL_SEND_FAILED);
        }
    }

    @Transactional
    public void verify(String rawToken) {
        EmailVerification verification = emailVerificationRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new BaseException(AuthResponseCode.EMAIL_VERIFICATION_TOKEN_INVALID));

        LocalDateTime now = LocalDateTime.now();
        if (verification.isExpired(now)) {
            throw new BaseException(AuthResponseCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!verification.isVerified()) {
            verification.verify(now);
        }
    }

    @Transactional(readOnly = true)
    public void requireVerified(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(AuthResponseCode.EMAIL_VERIFICATION_REQUIRED));

        if (!verification.isVerified()) {
            throw new BaseException(AuthResponseCode.EMAIL_VERIFICATION_REQUIRED);
        }
        if (verification.isExpired(LocalDateTime.now())) {
            throw new BaseException(AuthResponseCode.EMAIL_VERIFICATION_EXPIRED);
        }
    }

    @Transactional
    public void consumeVerified(String rawEmail) {
        emailVerificationRepository.deleteByEmail(normalizeEmail(rawEmail));
    }

    private String buildVerificationMail(String verificationUrl) {
        return """
                <!doctype html>
                <html lang="ko">
                <body style="font-family:Arial,sans-serif;color:#222;line-height:1.6;">
                  <h2>HSTEP 학교 이메일 인증</h2>
                  <p>HSTEP 회원가입을 위한 이메일 인증 요청입니다.</p>
                  <p>아래 버튼을 클릭하면 학교 이메일 인증이 자동으로 완료됩니다.</p>
                  <p style="margin:28px 0;">
                    <a href="%s" style="display:inline-block;padding:12px 24px;background:#144574;color:#fff;text-decoration:none;border-radius:6px;">확인하기</a>
                  </p>
                  <p>인증 링크는 %d분 동안 유효합니다.</p>
                  <p>본인이 요청하지 않았다면 이 메일을 무시해주세요.</p>
                </body>
                </html>
                """.formatted(verificationUrl, expirationMinutes);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String createToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BaseException(AuthResponseCode.EMAIL_VERIFICATION_TOKEN_INVALID);
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
