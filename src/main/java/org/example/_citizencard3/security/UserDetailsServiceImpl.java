package org.example._citizencard3.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.User;
import org.example._citizencard3.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Authenticating user with email: {}", email);
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("找不到使用者: " + email);
                });

        return buildUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new CustomException("找不到使用者ID: " + id, HttpStatus.NOT_FOUND);
                });

        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(User user) {
        validateUserStatus(user);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }

    private void validateUserStatus(User user) {
        if (!user.isActive()) {
            log.warn("Attempt to authenticate inactive user: {}", user.getEmail());
            throw new CustomException("帳號已被停用", HttpStatus.UNAUTHORIZED);
        }

        if (!user.isEmailVerified()) {
            log.warn("Attempt to authenticate user with unverified email: {}", user.getEmail());
            throw new CustomException("請先驗證您的電子郵件", HttpStatus.UNAUTHORIZED);
        }
    }
}
