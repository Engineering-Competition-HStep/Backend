package com.Hstep.Hstep.domain.member.dto;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.entity.MemberRole;
import com.Hstep.Hstep.domain.member.entity.MemberTrack;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MemberDto {

    @Getter
    @NoArgsConstructor
    public static class UpdateReq {

        @Size(min = 2, max = 30, message = "이름은 2자 이상 30자 이하여야 합니다.")
        private String name;

        @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
        @Max(value = 4, message = "학년은 4 이하여야 합니다.")
        private Integer grade;

        @DecimalMin(value = "0.0", message = "학점은 0.0 이상이어야 합니다.")
        @DecimalMax(value = "4.5", message = "학점은 4.5 이하여야 합니다.")
        private BigDecimal gpa;

        @Size(min = 1, max = 2, message = "트랙은 1개 이상 2개 이하로 선택해야 합니다.")
        private List<@NotNull @Positive Long> trackIds;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MemberRes {
        private String userId;
        private String email;
        private String name;
        private Integer grade;
        private BigDecimal gpa;
        private MemberRole role;
        private List<Long> trackIds;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static MemberRes fromEntity(Member member) {
            List<Long> trackIds = member.getMemberTracks().stream()
                    .map(MemberTrack::getTrackId)
                    .toList();

            return MemberRes.builder()
                    .userId(member.getUserId())
                    .email(member.getEmail())
                    .name(member.getName())
                    .grade(member.getGrade())
                    .gpa(member.getGpa())
                    .role(member.getRole())
                    .trackIds(trackIds)
                    .createdAt(member.getCreatedAt())
                    .updatedAt(member.getUpdatedAt())
                    .build();
        }
    }
}
