package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByName(String name);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<Member> findByBirthDateAndPhoneNumber(String birthDate, String phoneNumber);
    Optional<Member> findByEmailAndPhoneNumber(String email, String phoneNumber);
}
