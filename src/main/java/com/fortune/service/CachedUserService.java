package com.fortune.service;

import com.fortune.entity.User;
import com.fortune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 🚀 캐시를 사용하는 사용자 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachedUserService {

    private final UserRepository userRepository;

    /**
     * 사용자 조회 (캐시 적용)
     * 
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        log.info("🔍 데이터베이스에서 사용자 조회: {}", userId);
        return userRepository.findById(userId);
    }

    /**
     * 사용자명으로 조회 (캐시 적용)
     */
    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.info("🔍 데이터베이스에서 사용자 조회 (username): {}", username);
        return userRepository.findByUsernameAndDeletedAtIsNull(username);
    }

    /**
     * 사용자 정보 업데이트 (캐시 갱신)
     */
    @CachePut(value = "users", key = "#user.id")
    @Transactional
    public User updateUser(User user) {
        log.info("💾 사용자 정보 업데이트 및 캐시 갱신: {}", user.getId());
        return userRepository.save(user);
    }

    /**
     * 사용자 삭제 (캐시 제거)
     */
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public void deleteUser(Long userId) {
        log.info("🗑️ 사용자 삭제 및 캐시 제거: {}", userId);
        userRepository.deleteById(userId);
    }

    /**
     * 모든 사용자 캐시 제거
     */
    @CacheEvict(value = "users", allEntries = true)
    public void clearUserCache() {
        log.info("🧹 모든 사용자 캐시 제거");
    }
}
