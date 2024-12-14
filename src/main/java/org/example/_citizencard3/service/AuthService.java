package org.example._citizencard3.service;

import lombok.extern.slf4j.Slf4j;
import org.example._citizencard3.dto.request.LoginRequest;
import org.example._citizencard3.dto.request.RegisterRequest;
import org.example._citizencard3.dto.response.LoginResponse;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.User;
import org.example._citizencard3.model.Wallet;
import org.example._citizencard3.repository.UserRepository;
import org.example._citizencard3.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09\\d{8}$");

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            validateLoginRequest(request);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));

            if (!user.isActive()) {
                throw new CustomException("帳戶已被停用", HttpStatus.FORBIDDEN);
            }

            String token = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            return buildLoginResponse(user, token, refreshToken);
        } catch (BadCredentialsException e) {
            throw new CustomException("帳號或密碼錯誤", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("登入失敗", e);
            throw new CustomException("登入處理失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void logout(String jwtToken) {
        if (jwtToken != null) {
            try {
                jwtTokenProvider.invalidateToken(jwtToken);
                log.info("用戶登出成功");
            } catch (Exception e) {
                log.error("登出處理失敗", e);
                throw new CustomException("登出處理失敗", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    public UserResponse getProfile(String jwtToken) {
        try {
            String email = jwtTokenProvider.getEmailFromToken(jwtToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException("用戶不存在", HttpStatus.NOT_FOUND));
            return buildUserResponse(user);
        } catch (Exception e) {
            log.error("獲取用戶資料失敗", e);
            throw new CustomException("獲取用戶資料失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean validateToken(String jwtToken) {
        try {
            return jwtTokenProvider.validateToken(jwtToken);
        } catch (Exception e) {
            log.error("Token驗證失敗", e);
            return false;
        }
    }

    private LoginResponse buildLoginResponse(User user, String token, String refreshToken) {
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .emailVerified(user.isEmailVerified())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    private UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .role(user.getRole())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .version(user.getVersion())
                .build();
    }

    private void validateLoginRequest(LoginRequest request) {
        if (!StringUtils.hasText(request.getEmail()) || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new CustomException("無效的電子郵件格式", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new CustomException("密碼不能為空", HttpStatus.BAD_REQUEST);
        }
    }

    public boolean isEmailAvailable(String email) {
        if (!StringUtils.hasText(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new CustomException("無效的電子郵件格式", HttpStatus.BAD_REQUEST);
        }
        return !userRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Transactional
    public void updateLastLoginTime(String email) {
        try {
            userRepository.updateLastLoginTime(email, LocalDateTime.now());
            log.info("更新用戶 {} 最後登入時間成功", email);
        } catch (Exception e) {
            log.error("更新最後登入時間失敗", e);
            throw new CustomException("更新登入時間失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        try {
            validateRegistrationRequest(request);

            if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
                throw new CustomException("此電子郵件已被註冊", HttpStatus.CONFLICT);
            }

            LocalDateTime now = LocalDateTime.now();
            User user = createUserFromRequest(request, now);

            // 創建並關聯錢包
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(0.0);
            wallet.setCreatedAt(now);
            wallet.setUpdatedAt(now);
            user.setWallet(wallet);

            // 保存用戶和錢包
            user = userRepository.save(user);
            log.info("新用戶註冊成功: {}", user.getEmail());

            return buildUserResponse(user);
        } catch (CustomException e) {
            log.error("註冊失敗: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("註冊處理失敗", e);
            throw new CustomException("註冊處理失敗", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private User createUserFromRequest(RegisterRequest request, LocalDateTime now) {
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setBirthday(request.getBirthday());
        user.setGender(request.getGender());
        user.setRole("ROLE_USER");
        user.setActive(true);
        user.setEmailVerified(false);
        user.setLastLoginTime(now);
        user.setLastLoginIp("0.0.0.0");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setVersion(0);
        return user;
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getEmail()) || !EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new CustomException("無效的電子郵件格式", HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new CustomException("密碼不能為空", HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.hasText(request.getPhone()) && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new CustomException("無效的手機號碼格式", HttpStatus.BAD_REQUEST);
        }

        if (!StringUtils.hasText(request.getName()) || request.getName().length() < 2) {
            throw new CustomException("姓名長度必須至少為2個字符", HttpStatus.BAD_REQUEST);
        }

        if (request.getBirthday() == null) {
            throw new CustomException("生日不能為空", HttpStatus.BAD_REQUEST);
        }

        if (request.getGender() == null) {
            throw new CustomException("性別不能為空", HttpStatus.BAD_REQUEST);
        }
    }
}
