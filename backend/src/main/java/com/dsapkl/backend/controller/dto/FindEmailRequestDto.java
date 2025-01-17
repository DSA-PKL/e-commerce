package com.dsapkl.backend.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindEmailRequestDto {
    @NotEmpty(message = "생년월일은 필수입니다")
    @Pattern(regexp = "\\d{8}", message = "생년월일은 8자리 숫자(YYYYMMDD)로 입력해주세요")
    private String birthDate;

    @NotEmpty(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "올바른 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber;
}
