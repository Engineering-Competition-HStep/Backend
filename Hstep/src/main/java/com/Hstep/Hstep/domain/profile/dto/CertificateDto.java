package com.Hstep.Hstep.domain.profile.dto;

import com.Hstep.Hstep.domain.profile.entity.Certificate;
import java.time.LocalDateTime;

public class CertificateDto {

    public record Request(String certificateName, Integer issuedYear) {
    }

    public record Response(Long certificateId, String certificateName, Integer issuedYear,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response from(Certificate certificate) {
            return new Response(
                    certificate.getCertificateId(),
                    certificate.getCertificateName(),
                    certificate.getIssuedYear(),
                    certificate.getCreatedAt(),
                    certificate.getUpdatedAt()
            );
        }
    }
}