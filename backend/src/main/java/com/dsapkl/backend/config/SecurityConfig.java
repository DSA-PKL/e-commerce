package com.dsapkl.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import com.dsapkl.backend.service.MemberService;
import com.dsapkl.backend.entity.Member;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                         UserDetailsService userDetailsService,
                                         PasswordEncoder passwordEncoder) throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        http
            .authenticationProvider(authProvider)
            .csrf().disable()
            .authorizeHttpRequests((auth) -> auth
                // 모든 사용자가 접근 가능한 경로
                .requestMatchers(
                    "/", 
                    "/home",
                    "/error",
                    "/access-denied",
                    "/items/view/**",            // 상품 조회
                    "/reviews/view/**",          // 리뷰 조회
                    "/members/new",              // 회원가입
                    "/members/login",            // 로그인
                    "/members/find-*",           // 이메일/비밀번호 찾기
                    "/members/find-email-result",
                    "/members/find-password-result",
                    "/members/api/check-*",      // 이메일/전화번호 중복 체크
                    "/css/**", 
                    "/js/**", 
                    "/images/**",
                    "/assets/**"
                ).permitAll()
                // 관리자만 접근 가능한 경로
                .requestMatchers(
                    "/items/manage/**",             // 상품 관리 뷰
                    "/orders/manage/**",            // 주문 관리 뷰
                    "/members/manage/**",           // 회원 관리 뷰
                    "/reviews/manage/**",           // 리뷰 관리 뷰
                    "/api/items/manage/**",         // 상품 관리 API
                    "/api/orders/manage/**",        // 주문 관리 API
                    "/api/members/manage/**",       // 회원 관리 API
                    "/api/reviews/manage/**"        // 리뷰 관리 API
                ).hasAuthority("ADMIN")
                // 인증된 회원만 접근 가능한 경로
                .requestMatchers(
                    "/cart/**",                     // 장바구니
                    "/orders/**",                   // 주문 관련
                    "/mypage/**",                   // 마이페이지
                    "/reviews/write/**",            // 리뷰 작성
                    "/reviews/edit/**",             // 리뷰 수정
                    "/reviews/delete/**",           // 리뷰 삭제
                    "/members/update/**",           // 회원 정보 수정
                    "/api/cart/**",                 // 장바구니 API
                    "/api/orders/**",               // 주문 API
                    "/api/reviews/**",              // 리뷰 API
                    "/api/member-info/**"           // 회원 정보 API
                ).authenticated()
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/members/login")
                .loginProcessingUrl("/members/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/members/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling((exception) -> exception
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}