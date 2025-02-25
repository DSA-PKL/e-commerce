package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Address;
import com.dsapkl.backend.entity.Cart;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.entity.Role;
import com.dsapkl.backend.repository.CartRepository;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.MemberRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final EmailService emailService;
    private final MemberInfoRepository memberInfoRepository;
    private final UserActivityLogService logService;

    @Transactional
    public Long join(Member member) {
        validateDuplicateMember(member);
        
        boolean isFirstMember = memberRepository.count() == 0;
        if (isFirstMember) {
            member.changeRole(Role.ADMIN.name());
        } else {
            member.changeRole(Role.USER.name());
        }
        
        Member savedMember = memberRepository.save(member);
        
        // 장바구니와 회원정보 생성은 회원 저장 후에
        Cart cart = Cart.createCart(savedMember);
        cartRepository.save(cart);

        MemberInfo memberInfo = MemberInfo.createMemberInfo(savedMember);
        memberInfoRepository.save(memberInfo);

        // 회원가입 로그는 모든 저장이 완료된 후에
        try {
            logService.logEvent(savedMember.getId(), "MEMBER_REGISTER", 
                String.format("New member registered: %s", savedMember.getEmail()));
        } catch (Exception e) {
            // 로그 저장 실패는 회원가입에 영향을 주지 않도록
        }

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
    @Transactional
    public Member login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
            .filter(m -> m.getPassword().equals(password))
            .orElse(null);
            
        if (member != null && member.getRole() != Role.ADMIN) {
            try {
                logService.logEvent(member.getId(), "MEMBER_LOGIN", 
                    String.format("Member logged in: %s", email));
            } catch (Exception e) {
                // 로그 저장 실패는 로그인에 영향을 주지 않도록
            }
        }
        
        return member;
    }

    public Member findMember(Long id) {
        return memberRepository.findById(id).get();
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    //이메일 체크
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !memberRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean isPhoneAvailable(String phoneNumber) {
        return !memberRepository.existsByPhoneNumber(phoneNumber);
    }

    public String findEmailByBirthDateAndPhone (String birthDate, String phoneNumber) {
        Member member = memberRepository.findByBirthDateAndPhoneNumber(birthDate, phoneNumber).orElseThrow(() -> new IllegalArgumentException("No matching member information found."));
        return member.getEmail();
    }

    @Transactional
    public void sendTemporaryPassword(String email, String phoneNumber) {
        Member member = memberRepository.findByEmailAndPhoneNumber(email, phoneNumber).orElseThrow(()-> new IllegalStateException("No matching member information found."));

        String temporaryPassword = generateTemporaryPassword();
        member.updatePassword(temporaryPassword);

        emailService.sendTemporaryPassword(email, temporaryPassword);
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 회원 정보 업데이트
     */
    @Transactional
    public void updateMember(Long memberId, String name, String email, 
                           String phoneNumber, String birthDate, Address address) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        // 이메일과 전화번호 중복 체크 (자신의 것은 제외)
        Member existingMemberByEmail = memberRepository.findByEmail(email).orElse(null);
        if (existingMemberByEmail != null && !existingMemberByEmail.getId().equals(memberId)) {
            throw new IllegalStateException("Email is already in use");
        }

        if (!member.getPhoneNumber().equals(phoneNumber) && 
            memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalStateException("Phone number is already in use");
        }

        // Member 엔티티에 updateMemberInfo 메서드 추가 필요
        member.updateMemberInfo(name, email, phoneNumber, birthDate, address);
        memberRepository.save(member);
        
        // 회원정보 수정 로그
        logService.logEvent(memberId, "MEMBER_UPDATE", 
            String.format("Member information updated: %s", email));
    }

    public boolean isAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        log.info("Checking admin status for member: {}, role: {}", memberId, member.getRole());
        return member.getRole() == Role.ADMIN;
    }

    @Transactional
    public void logout(Long memberId) {
        try {
            logService.logEvent(memberId, "MEMBER_LOGOUT", "Member logged out");
        } catch (Exception e) {
            // 로그 저장 실패는 로그아웃에 영향을 주지 않도록
        }
    }
}
