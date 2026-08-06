package org.spring.backend.member.jwt;

import lombok.RequiredArgsConstructor;
import org.spring.backend.member.entity.MemberEntity;
import org.spring.backend.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        MemberEntity memberEntity = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다."+userEmail));
        return new CustomUserDetails(memberEntity);
    }
}
