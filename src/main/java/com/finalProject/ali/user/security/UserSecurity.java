package com.finalProject.ali.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class UserSecurity {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // UserSecurity.java 수정 제안
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form
                        .loginPage("/user/login") // 권한 없을 때 이동할 페이지 (GET 요청)
                        .loginProcessingUrl("/doLogin_dummy") // 중요: Security가 가로채지 못하게 가짜 주소 입력
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
//                        // permitAll에 /user/index와 /user/logout이 잘 포함되어 있는지 확인
//                        .requestMatchers("/user/register", "/user/signup", "/user/login",
//                                "/user/update","/user/index", "/user/logout",
//                                "/user/switch-role","/user/supplier-signup",
//                                "/supplier/**").permitAll()
//                        .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )
                // 아래 로그아웃 설정을 추가하면 세션 정리가 더 확실해집니다.
                .logout(logout -> logout
                        .logoutUrl("/user/logout") // 로그아웃을 처리할 URL
                        .logoutSuccessUrl("/user/index") // 로그아웃 성공 후 이동할 페이지
                        .invalidateHttpSession(true) // 세션 삭제 필수
                        .deleteCookies("JSESSIONID") // 쿠키 삭제로 세션 꼬임 방지
                );

        return http.build();
    }
}