package com.dsapkl.backend.controller.dto;

import com.dsapkl.backend.entity.Interest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberInfoCreateDto {
    private Integer age;
    private String gender;
    private Interest interests;
} 