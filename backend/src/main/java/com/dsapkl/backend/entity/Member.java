package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @Embedded
    private Address address;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(name = "birth_date", length = 8, nullable = false)
    private String birthDate;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Builder
    private Member(String name, String email, String password, Address address, Role role,
                   String birthDate, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.role = role;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
    }

    public void changeRole(String role) {
        if (role.equals("admin")) {
            this.role = Role.ADMIN;
        } else {
            this.role = Role.USER;
        }
    }

    public void updatePassword(String password) {
        this.password = password;
    }

}
