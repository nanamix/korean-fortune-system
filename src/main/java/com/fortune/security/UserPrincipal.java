package com.fortune.security;
import com.fortune.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
/**
 * 🔐 사용자 인증 주체 클래스
 * 
 * <p>Spring Security에서 사용하는 사용자 정보를 담는 클래스입니다.</p>
 * <p>UserDetails와 OAuth2User 인터페이스를 모두 구현하여 
 * 일반 로그인과 소셜 로그인을 통합 처리합니다.</p>
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
public class UserPrincipal implements OAuth2User, UserDetails {
    private Long id;
    private String email;
    private String password;
    private String name;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    /**
     * 🏗️ 생성자
     * 
     * @param id 사용자 ID
     * @param email 이메일
     * @param password 비밀번호
     * @param name 이름
     * @param authorities 권한 목록
     * @param enabled 계정 활성화 여부
     * @param accountNonExpired 계정 만료 여부
     * @param accountNonLocked 계정 잠금 여부
     * @param credentialsNonExpired 자격증명 만료 여부
     */
    public UserPrincipal(Long id, String email, String password, String name,
                        Collection<? extends GrantedAuthority> authorities,
                        boolean enabled, boolean accountNonExpired, 
                        boolean accountNonLocked, boolean credentialsNonExpired) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }
    /**
     * 🔧 사용자 엔티티로부터 UserPrincipal 생성
     * 
     * @param user 사용자 엔티티
     * @return UserPrincipal 인스턴스
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getName(),
            authorities,
            true, // enabled
            true, // accountNonExpired
            true, // accountNonLocked
            true  // credentialsNonExpired
        );
    }
    /**
     * 🔧 OAuth2 사용자 정보로부터 UserPrincipal 생성
     * 
     * @param user 사용자 엔티티
     * @param attributes OAuth2 속성
     * @return UserPrincipal 인스턴스
     */
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }
    // ========== UserDetails 인터페이스 구현 ==========
    /**
     * 사용자 비밀번호 조회
     * 
     * @return 사용자 비밀번호
     */
    @Override
    public String getPassword() {
        return password;
    }
    /**
     * 사용자 이메일 조회
     * 
     * @return 사용자 이메일
     */
    @Override
    public String getUsername() {
        return email;
    }
    /**
     * 사용자 계정 만료 여부 조회
     * 
     * @return 사용자 계정 만료 여부
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    /**
     * 사용자 계정 잠금 여부 조회
     * 
     * @return 사용자 계정 잠금 여부
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    /**
     * 사용자 자격증명 만료 여부 조회
     * 
     * @return 사용자 자격증명 만료 여부
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    /**
     * 사용자 활성화 여부 조회
     * 
     * @return 사용자 활성화 여부
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    /**
     * 사용자 권한 조회
     * 
     * @return 사용자 권한
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    // ========== OAuth2User 인터페이스 구현 ==========
    /**
     * 사용자 정보 조회
     * 
     * @return 사용자 정보
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    /**
     * 사용자 이름 조회
     * 
     * @return 사용자 이름
     */
    @Override
    public String getName() {
        return String.valueOf(id);
    }
    // ========== Getter/Setter ==========
    /**
     * 사용자 ID 조회
     * 
     * @return 사용자 ID
     */
    public Long getId() {
        return id;
    }
    /**
     * 사용자 이메일 조회
     * 
     * @return 사용자 이메일
     */
    public String getEmail() {
        return email;
    }
    /**
     * 사용자 이름 조회
     * 
     * @return 사용자 이름
     */
    public String getDisplayName() {
        return name;
    }
    /**
     * 사용자 정보 설정
     * 
     * @param attributes 사용자 정보
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    // ========== Object 메서드 오버라이드 ==========
    /**
     * 사용자 정보 비교
     * 
     * @param o 비교 객체
     * @return 비교 결과
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return id.equals(that.id);
    }
    /**
     * 사용자 정보 해시 코드
     * 
     * @return 사용자 정보 해시 코드
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    /**
     * 사용자 정보 문자열 표현
     * 
     * @return 사용자 정보 문자열 표현
     */
    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", authorities=" + authorities +
                ", enabled=" + enabled +
                '}';
    }
}