package com.dsapkl.backend.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
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

}