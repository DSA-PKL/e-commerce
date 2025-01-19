package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Cart;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.repository.CartRepository;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.MemberRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final EmailService emailService;
    private final MemberInfoRepository memberInfoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    //회원가입
    @Transactional(readOnly = false)
    public Long join(Member member) {
        validateDuplicateMember(member);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        Member savedMember = memberRepository.save(member);

        Cart cart = Cart.createCart(savedMember);
        cartRepository.save(cart);

        MemberInfo memberInfo = MemberInfo.createMemberInfo(savedMember);
        memberInfoRepository.save(memberInfo);

        return savedMember.getId();
    }

    //중복 회원 검증
    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail()).orElse(null);
        if (findMember != null) {
            throw new IllegalStateException("Member already exists.");
        }

        if (memberRepository.existsByPhoneNumber(member.getPhoneNumber())) {
            throw new IllegalStateException("Phone number is already in use.");
        }
    }

    //로그인 체크
    public Member login(String email, String password) {

        Member member = memberRepository.findByEmail(email).orElse(null);

        if (member != null && passwordEncoder.matches(password, member.getPassword())) {
            return member;
        }

            return null;
    }

    public Member findMember(Long id) {
        return memberRepository.findById(id).get();
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    //이메일 체크
    public boolean isEmailAvailable(String email) {
       return memberRepository.existsByEmail(email);
    }

    public boolean isPhoneAvailable(String phoneNumber) {
        return memberRepository.existsByPhoneNumber(phoneNumber);
    }

    public String findEmailByBirthDateAndPhone (String birthDate, String phoneNumber) {
        Member member = memberRepository.findByBirthDateAndPhoneNumber(birthDate, phoneNumber).orElseThrow(() -> new IllegalArgumentException("No matching member information found."));
        return member.getEmail();
    }

    @Transactional
    public void sendTemporaryPassword(String email, String phoneNumber) {
        Member member = memberRepository.findByEmailAndPhoneNumber(email, phoneNumber).orElseThrow(()-> new IllegalStateException("No matching member information found."));

        String temporaryPassword = generateTemporaryPassword();

        emailService.sendTemporaryPassword(email, temporaryPassword);

        member.updatePassword(passwordEncoder.encode(temporaryPassword));
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
