package com.Hstep.Hstep.domain.auth.service;

import com.Hstep.Hstep.domain.auth.entity.EmailVerification;
import com.Hstep.Hstep.domain.auth.repository.EmailVerificationRepository;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JavaMailSender mailSender;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(emailVerificationRepository, memberRepository, mailSender);
        ReflectionTestUtils.setField(service, "fromAddress", "hstep@example.com");
        ReflectionTestUtils.setField(service, "verifyBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "expirationMinutes", 15L);
    }

    @Test
    void sendVerificationStoresTokenAndSendsMail() {
        when(memberRepository.existsByEmail("student@hansung.ac.kr")).thenReturn(false);
        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.sendVerification("Student@hansung.ac.kr");

        ArgumentCaptor<EmailVerification> entityCaptor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationRepository).deleteByEmail("student@hansung.ac.kr");
        verify(emailVerificationRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getEmail()).isEqualTo("student@hansung.ac.kr");
        assertThat(entityCaptor.getValue().getTokenHash()).hasSize(64);

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getTo()).containsExactly("student@hansung.ac.kr");
        assertThat(mailCaptor.getValue().getText()).contains("/api/auth/email-verifications/verify?token=");
    }

    @Test
    void verifyMarksVerificationAsVerified() throws Exception {
        String token = "test-token";
        EmailVerification verification = EmailVerification.create(
                "student@hansung.ac.kr",
                sha256(token),
                LocalDateTime.now().plusMinutes(10)
        );
        when(emailVerificationRepository.findByTokenHash(sha256(token)))
                .thenReturn(Optional.of(verification));

        service.verify(token);

        assertThat(verification.isVerified()).isTrue();
    }

    @Test
    void consumeVerifiedDeletesVerification() {
        service.consumeVerified("Student@hansung.ac.kr");
        verify(emailVerificationRepository).deleteByEmail("student@hansung.ac.kr");
    }

    private String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
