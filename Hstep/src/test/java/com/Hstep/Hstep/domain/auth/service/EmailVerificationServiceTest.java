package com.Hstep.Hstep.domain.auth.service;

import com.Hstep.Hstep.domain.auth.entity.EmailVerification;
import com.Hstep.Hstep.domain.auth.repository.EmailVerificationRepository;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Properties;

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
    void sendVerificationStoresTokenAndSendsHtmlMail() throws Exception {
        when(memberRepository.existsByEmail("student@hansung.ac.kr")).thenReturn(false);
        when(emailVerificationRepository.save(any(EmailVerification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendVerification("Student@hansung.ac.kr");

        ArgumentCaptor<EmailVerification> entityCaptor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(emailVerificationRepository).deleteByEmail("student@hansung.ac.kr");
        verify(emailVerificationRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getEmail()).isEqualTo("student@hansung.ac.kr");
        assertThat(entityCaptor.getValue().getTokenHash()).hasSize(64);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("student@hansung.ac.kr");
        assertThat(mimeMessage.getSubject()).contains("HSTEP");
        assertThat(mimeMessage.getContent().toString()).contains("확인하기");
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
