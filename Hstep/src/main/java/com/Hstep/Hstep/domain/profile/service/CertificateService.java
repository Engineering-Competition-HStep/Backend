package com.Hstep.Hstep.domain.profile.service;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import com.Hstep.Hstep.domain.profile.dto.CertificateDto;
import com.Hstep.Hstep.domain.profile.entity.Certificate;
import com.Hstep.Hstep.domain.profile.exception.ProfileResponseCode;
import com.Hstep.Hstep.domain.profile.repository.CertificateRepository;
import com.Hstep.Hstep.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(String userId, CertificateDto.Request request) {
        Member member = memberRepository.getReferenceById(userId);
        Certificate certificate = new Certificate(request.certificateName(), request.issuedYear(), member);
        return certificateRepository.save(certificate).getCertificateId();
    }

    public List<CertificateDto.Response> findAllByUser(String userId) {
        return certificateRepository.findByMember_UserId(userId).stream()
                .map(CertificateDto.Response::from)
                .toList();
    }

    @Transactional
    public void update(Long certificateId, CertificateDto.Request request) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new BaseException(ProfileResponseCode.CERTIFICATE_NOT_FOUND));
        certificate.update(request.certificateName(), request.issuedYear());
    }

    @Transactional
    public void delete(Long certificateId) {
        if (!certificateRepository.existsById(certificateId)) {
            throw new BaseException(ProfileResponseCode.CERTIFICATE_NOT_FOUND);
        }
        certificateRepository.deleteById(certificateId);
    }
}