package com.dsapkl.backend.config;

import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        Member member = memberRepository.findByEmail(email).orElseThrow(()->{return new UsernameNotFoundException(email + " not found");
        });

        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .address(member.getAddress())
                .role(member.getRole())
                .birthDate(member.getBirthDate())
                .phoneNumber(member.getPhoneNumber())
                .build();

        return user;
    }
}
