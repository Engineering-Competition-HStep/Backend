package com.Hstep.Hstep.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailVerificationDto {

    public record SendRequest(
            @NotBlank(message = "학교 이메일은 필수 입력 값입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Pattern(
                    regexp = "^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)*hansung\\.ac\\.kr$",
                    message = "한성대학교 이메일만 사용할 수 있습니다."
            )
            String email
    ) {}
}
