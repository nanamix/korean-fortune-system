package com.fortune.service;

import com.fortune.entity.User;
import com.fortune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * 📧 이메일로 사용자 조회
 * <p>이메일을 사용하여 사용자 정보를 조회하고, Spring Security의 UserDetails를 반환합니다.</p>
 * <p>이 서비스는 사용자 인증 시 이메일을 기반으로 사용자 정보를 로드하는 데 사용됩니다.</p>
 * <p>사용자 정보는 UserRepository를 통해 데이터베이스에서 조회됩니다.</p>
 * <p>이메일로 사용자 정보를 조회하는 경우, 사용자 이름(username) 대신 이메일을 사용합니다.</p>
 *
 * <p>예외 처리: 사용자가 존재하지 않을 경우 UsernameNotFoundException을 발생시킵니다.</p>
 *
 * @author 하진영
 */
@Service
@RequiredArgsConstructor
public class EmailUserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // 이메일을 username으로 사용
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isEnabled())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
