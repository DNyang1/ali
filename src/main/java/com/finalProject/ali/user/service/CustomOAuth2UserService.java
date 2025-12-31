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
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어떤 소셜 서비스인지 확인 (google, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String userId = "";
        String name = "";
        String email = "";

        if ("google".equals(registrationId)) {
            // 구글 데이터 추출
            userId = "google_" + attributes.get("sub"); // 구글의 고유 식별값은 'sub'
            name = (String) attributes.get("name");
            email = (String) attributes.get("email");
        } else if ("kakao".equals(registrationId)) {
            // 기존 카카오 로직
            userId = "kakao_" + attributes.get("id");
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            name = (String) properties.get("nickname");
            email = userId + "@kakao.com";
        }

        // DB 저장 및 세션 처리 로직 (기존과 동일)
        UserDTO user = userDAO.findByUserId(userId);
        if (user == null) {
            user = new UserDTO();
            user.setUserId(userId);
            user.setName(name);
            user.setEmail(email);
            user.setPassword("OAUTH_USER");
            userDAO.insertUser(user);
        }
        session.setAttribute("loginUser", user);

        return oAuth2User;

    }
}