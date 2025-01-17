package com.dsapkl.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTemporaryPassword(String to, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[PKLshop] 임시 비밀번호 발급");
        message.setText("임시 비밀번호: " + temporaryPassword + "로그인 후 반드시 비밀번호를 변경해주세요.");
        mailSender.send(message);
    }
}
