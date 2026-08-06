package org.spring.backend.member.jwt;

import lombok.RequiredArgsConstructor;
import org.spring.backend.member.enumtype.Role;
import org.spring.backend.member.entity.MemberAddEntity;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
//oauth2 소셜로그인을 위한 커스텀 서비스
public class CustomDefaultOAuth2UserService extends DefaultOAuth2UserService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        //소셜로그인의 고유 아이디(구글,네이버,카카오)
        String registrationId = clientRegistration.getRegistrationId();
        return oAuth2UserSuccess(oAuth2User, registrationId);
    }

    //소셜로그인 성공 시 반환할 값 설정
    private OAuth2User oAuth2UserSuccess(OAuth2User oAuth2User, String registrationId) {
        String userEmail = "";
        String userName = "";

        String oauth2Dummy = "OAuth2_USER_DUMMY_PASSWORD";

        //각 소셜로그인별로 처리값이 다르기때문에 조건문으로 따로 처리
        if(registrationId.equals("google")){
            userEmail = oAuth2User.getAttribute("email");
            userName = oAuth2User.getAttribute("name");
        }else if(registrationId.equals("naver")){
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            userEmail = (String) response.get("email");
            userName = (String) response.get("name");
        }else if(registrationId.equals("kakao")){
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
            userEmail = (String) kakaoAccount.get("email");
            userName = (String) kakaoProfile.get("nickname");
        }
        if(userEmail == null || userEmail.isEmpty()){
            throw new OAuth2AuthenticationException("이메일 정보가 없습니다.");
        }
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByUserEmail(userEmail);
        if(optionalMemberEntity.isPresent()){
            return new CustomUserDetails(optionalMemberEntity.get(), oAuth2User.getAttributes());
        }
        //계정정보, 임시데이터로 멤버엔티티 작성
        MemberEntity memberEntity = MemberEntity.builder()
                .userEmail(userEmail)
                .userPw(passwordEncoder.encode(oauth2Dummy))
                .userName(userName)
                .role(Role.MEMBER)
                .subscribe(0)
                .profilePhoto(0)
                .build();


        //저장과 동시에 저장용 데이터 생성
        MemberEntity member = memberRepository.save(memberEntity);
        //추가 멤버데이터 저장을 위해 더미데이터 생성
        MemberAddEntity memberAdd = MemberAddEntity.createDefault();
        member.setMemberAddEntity(memberAdd);
        //추가 멤버데이터까지 새로 저장
        memberRepository.save(memberEntity);
        return new CustomUserDetails(memberEntity, oAuth2User.getAttributes());
    }
}
