package com.dsapkl.backend.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotEmpty(message = "이메일 주소는 필수 입니다.")
    @Email
    private String email;

    @NotEmpty(message = "비밀번호는 필수 입니다.")
    private String password;
}
