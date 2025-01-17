package com.dsapkl.backend.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberForm {

    @NotEmpty(message = "이름은 필수 입니다.")
    private String name;

    @NotEmpty(message = "이메일 필수 입니다.")
    @Email
    private String email;

    @NotEmpty(message = "비밀번호 필수 입니다.")
    private String password;

    private String city;
    private String street;
    private String zipcode;

    private String role;

    @NotEmpty(message = "생년월일은 필수 입니다.")
    @Pattern(regexp = "\\d{8}", message = "생년월일은 8자리 숫자(YYYYMMDD)로 입력해주세요.")
    private String birthDate;
    @NotEmpty(message = "휴대폰 번호는 필수 입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)")
    private String phoneNumber;
}