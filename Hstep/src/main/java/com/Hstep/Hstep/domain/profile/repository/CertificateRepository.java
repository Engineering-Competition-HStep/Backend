package com.Hstep.Hstep.domain.profile.repository;

import com.Hstep.Hstep.domain.profile.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByMember_UserId(String userId);
    Optional<Certificate> findByCertificateIdAndMember_UserId(Long certificateId, String userId);
}