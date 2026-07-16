package com.Hstep.Hstep.domain.auth.dto;

import com.Hstep.Hstep.domain.member.dto.MemberDto.MemberRes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class AuthDto {

    @Getter
    @NoArgsConstructor
    public static class SignupReq {

        @NotNull(message = "학번은 필수 입력 값입니다.")
        @Min(value = 1_000_000L, message = "학번 형식을 확인해주세요.")
        @Max(value = 9_999_999_999L, message = "학번 형식을 확인해주세요.")
        private Long userId;

        @NotBlank(message = "학교 이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)*hansung\\.ac\\.kr$",
                message = "한성대학교 이메일만 사용할 수 있습니다."
        )
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "비밀번호는 영문과 숫자를 포함한 8자 이상 64자 이하여야 합니다."
        )
        private String password;

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 2, max = 30, message = "이름은 2자 이상 30자 이하여야 합니다.")
        private String name;

        @NotNull(message = "학년은 필수 입력 값입니다.")
        @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
        @Max(value = 4, message = "학년은 4 이하여야 합니다.")
        private Integer grade;

        @NotEmpty(message = "소속 트랙을 하나 이상 선택해주세요.")
        @Size(max = 2, message = "트랙은 최대 2개까지 선택할 수 있습니다.")
        private List<@NotNull @Positive Long> trackIds;
    }

    @Getter
    @NoArgsConstructor
    public static class LoginReq {

        @NotNull(message = "학번을 입력해주세요.")
        @Positive(message = "학번 형식을 확인해주세요.")
        private Long userId;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    public static class ChangePasswordReq {

        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "새 비밀번호는 영문과 숫자를 포함한 8자 이상 64자 이하여야 합니다."
        )
        private String newPassword;
    }

    @Getter
    @AllArgsConstructor
    public static class CheckAvailableRes {
        private boolean available;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TokenRes {
        private String accessToken;
        private String tokenType;
        private long expiresIn;
        private MemberRes member;

        public static TokenRes of(String accessToken, long expiresIn, MemberRes member) {
            return TokenRes.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(expiresIn)
                    .member(member)
                    .build();
        }
    }
}
