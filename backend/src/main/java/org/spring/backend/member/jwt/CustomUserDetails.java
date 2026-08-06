package org.spring.backend.member.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.spring.backend.member.entity.MemberEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;


//@RequiredArgsConstructor
@Getter
@Setter
public class CustomUserDetails implements OAuth2User, UserDetails {
    private final MemberEntity memberEntity;

    //oauth2 관리
    private Map<String, Object> getAttributes;

    //일반 로그인용 생성자
    public CustomUserDetails(MemberEntity memberEntity){
        this.memberEntity = memberEntity;
    }

    //oauth2 로그인용 생성자
    public CustomUserDetails(MemberEntity memberEntity, Map<String,Object> getAttributes){
        this.memberEntity = memberEntity;
        this.getAttributes = getAttributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.getAttributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberEntity.getRole().toString();
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return memberEntity.getUserPw();
    }

    @Override
    public String getUsername() {
        return memberEntity.getUserEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return memberEntity.getUserName();
    }
}
