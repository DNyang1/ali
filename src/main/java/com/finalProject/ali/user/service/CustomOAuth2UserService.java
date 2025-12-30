package com.finalProject.ali.user.service;


import com.finalProject.ali.user.dao.UserDAO;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 유저 정보 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 카카오에서 전달받은 속성(attributes) 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 고유 ID (문자열로 변환)
        String kakaoId = String.valueOf(attributes.get("id"));

        // properties 내 닉네임, 프로필 이미지 정보
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String nickname = (String) properties.get("nickname");

        // 3. 우리 서비스 전용 ID 생성 (중복 방지용 접두사)
        String userId = "kakao_" + kakaoId;

        // 4. DB 조회 및 자동 회원가입 로직
        UserDTO user = userDAO.findByUserId(userId);

        if (user == null) {
            // 가입된 정보가 없으면 신규 생성
            user = new UserDTO();
            user.setUserId(userId);
            user.setName(nickname);
            user.setPassword("OAUTH_USER"); // 소셜 로그인은 비번이 필요 없으므로 임의값 세팅
            user.setEmail(kakaoId + "@kakao.com"); // 실제 이메일 권한이 없다면 가공해서 저장

            userDAO.insertUser(user);
            System.out.println("신규 카카오 유저 가입 완료: " + userId);
        }

        // 5. 세션 유지 (기존 컨트롤러 로그인 방식과 일치시킴)
        session.setAttribute("loginUser", user);

        return oAuth2User;
    }
}