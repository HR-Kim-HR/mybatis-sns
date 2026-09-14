package net.likelion.bebc25.sns.security.service;

import net.likelion.bebc25.sns.domain.Member;
import net.likelion.bebc25.sns.mapper.MemberMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    public CustomUserDetailsService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberMapper.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("사용자가 없습니다.");
        }
        return new net.likelion.bebc25.sns.security.principal.CustomUserDetails(member);
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new UsernameNotFoundException("사용자가 없습니다.");
        }
        return new net.likelion.bebc25.sns.security.principal.CustomUserDetails(member);
    }

}
