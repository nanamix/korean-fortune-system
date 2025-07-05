package com.fortune.service;
import com.fortune.entity.User;
import com.fortune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.stream.Collectors;
/**
 * 👤 사용자 세부 정보 서비스
 * 
 * <p>Spring Security에서 사용자 인증 시 사용자 정보를 조회합니다.</p>
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    /**
     * 사용자 리포지토리
     * - Autowired 어노테이션을 사용하여 사용자 리포지토리를 주입합니다.
     * - UserRepository 클래스를 사용하여 사용자 정보를 조회합니다.
     */
    private final UserRepository userRepository;
    /**
     * 사용자 조회
     * 
     * @param username 사용자명
     * @return 사용자 정보
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("🔍 사용자 조회 시도: {}", username);
        /* 사용자 조회 */
        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> {
                    log.error("❌ 사용자를 찾을 수 없음: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });
        log.debug("✅ 사용자 조회 성공: {}", username);
        return createUserPrincipal(user);
    }
    /**
     * User 엔티티로부터 UserDetails 생성
     * 
     * @param user 사용자 정보
     * @return 사용자 정보
     */
    private UserDetails createUserPrincipal(User user) {
        /* 권한 추출 */
        Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isEnabled())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
