package com.ssafy.ohmarking.common.auth;

import com.ssafy.ohmarking.db.entity.OMR;
import com.ssafy.ohmarking.db.entity.User;
import com.ssafy.ohmarking.db.repository.OMRRepository;
import com.ssafy.ohmarking.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
@Slf4j
@RequiredArgsConstructor
@Service
public class UserOAuth2Service extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OMRRepository omrRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakao_account.get("email");

        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String name = (String) properties.get("nickname");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            String introduction = "자기소개를 입력해주세요.";
            log.info("가입되지 않은 사용자입니다. DB에 저장합니다.");
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .codedEmail(encodeEmail(email))
                    .introduction(introduction)
                    .build();
            userRepository.save(newUser);
            omrRepository.save(OMR.builder()
                    .pageNum(1)
                    .color(0)
                    .user(newUser)
                    .build());
        }

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_MEMBER")), attributes, "id");
    }

    private String encodeEmail(String email){
        LocalDateTime now = LocalDateTime.now();
        int hash = 17;
        hash = 31 * hash + email.hashCode();
        hash = 31 * hash + now.hashCode();
        hash = hash & 0x7fffffff;

        return "OMR-" + hash;
    }

}