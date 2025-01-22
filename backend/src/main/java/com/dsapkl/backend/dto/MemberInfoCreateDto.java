package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.Interest;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.MemberInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberInfoCreateDto {
    private Integer age;
    private String gender;
    private Interest interests;
    
    // Member 기본 정보 추가
    private String name;
    private String email;
    private String phoneNumber;
    private String birthDate;
    
    // 주소 정보 추가
    private String city;
    private String street;
    private String zipcode;

    // 기존 정보로 DTO 생성하는 정적 메서드 추가
    public static MemberInfoCreateDto from(Member member, MemberInfo memberInfo) {
        MemberInfoCreateDto dto = new MemberInfoCreateDto();
        // Member 정보 설정
        dto.setName(member.getName());
        dto.setEmail(member.getEmail());
        dto.setPhoneNumber(member.getPhoneNumber());
        dto.setBirthDate(member.getBirthDate());
        
        // Address 정보 설정
        if (member.getAddress() != null) {
            dto.setCity(member.getAddress().getCity());
            dto.setStreet(member.getAddress().getStreet());
            dto.setZipcode(member.getAddress().getZipcode());
        }
        
        // MemberInfo 정보 설정
        dto.setAge(memberInfo.getAge());
        dto.setGender(memberInfo.getGender());
        dto.setInterests(memberInfo.getInterests());
        
        return dto;
    }
} 