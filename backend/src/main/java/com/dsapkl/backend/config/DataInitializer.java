package com.dsapkl.backend.config;

import com.dsapkl.backend.entity.Address;
import com.dsapkl.backend.entity.Cart;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Role;
import com.dsapkl.backend.repository.CartRepository;
import com.dsapkl.backend.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(MemberRepository memberRepository, CartRepository cartRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if (!memberRepository.existsByEmail("admin@example.com")) {

            Address address = new Address("city", "street", "000000");

            Member admin = new Member();
            admin.setName("Admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("1231234"));
            admin.setAddress(address);
            admin.setRole(Role.ADMIN);
            admin.setBirthDate("19800101");
            admin.setPhoneNumber("010-1234-5678");

            Member savedMember = memberRepository.save(admin);
            System.out.println("Admin account created!");

            Cart cart = Cart.createCart(savedMember);
            cartRepository.save(cart);
        }
    }
}
