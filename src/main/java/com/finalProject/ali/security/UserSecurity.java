package com.finalProject.ali.security;

import com.finalProject.ali.user.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class UserSecurity {
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

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
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/user/login") // 로그인 페이지 지정
                        .defaultSuccessUrl("/user/index") // 성공 시 이동할 곳
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )

                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/user/register", "/user/signup", "/user/login",
//                                "/user/update","/user/index", "/user/logout",
//                                "/user/switch-role","/user/supplier-signup",
//                                "/supplier/**").permitAll()
//                        .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/user/logout") // 로그아웃을 처리할 URL
                        .logoutSuccessUrl("/user/index") // 로그아웃 성공 후 이동할 페이지
                        .invalidateHttpSession(true) // 세션 삭제 필수
                        .deleteCookies("JSESSIONID") // 쿠키 삭제로 세션 꼬임 방지
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/user/login?expired=true")
                        .sessionRegistry(sessionRegistry()) // [중요] 세션 기록부 등록
                );

        return http.build();
    }
}