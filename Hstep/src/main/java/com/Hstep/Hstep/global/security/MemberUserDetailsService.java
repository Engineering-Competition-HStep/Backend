package com.Hstep.Hstep.global.security;

import com.Hstep.Hstep.domain.member.entity.Member;
import com.Hstep.Hstep.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long userId = Long.valueOf(username);
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
            return MemberPrincipal.from(member);
        } catch (NumberFormatException exception) {
            throw new UsernameNotFoundException("학번 형식이 올바르지 않습니다.", exception);
        }
    }
}
