package com.Hstep.Hstep.domain.profile.dto;

import com.Hstep.Hstep.domain.profile.entity.UserGradeGpa;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserGradeGpaDto {

    public record Request(Integer grade, BigDecimal gpa) {
    }

    public record Response(Long userGradeGpaId, Integer grade, BigDecimal gpa,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response from(UserGradeGpa entity) {
            return new Response(
                    entity.getUserGradeGpaId(),
                    entity.getGrade(),
                    entity.getGpa(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        }
    }
}