package com.Hstep.Hstep.domain.auth.repository;

import com.Hstep.Hstep.domain.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findByTokenHash(String tokenHash);
    void deleteByEmail(String email);
}
