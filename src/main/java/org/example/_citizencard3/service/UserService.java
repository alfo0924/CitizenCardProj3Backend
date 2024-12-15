package org.example._citizencard3.service;

import lombok.RequiredArgsConstructor;
import org.example._citizencard3.dto.request.UpdateProfileRequest;
import org.example._citizencard3.dto.response.UserResponse;
import org.example._citizencard3.exception.CustomException;
import org.example._citizencard3.model.User;
import org.example._citizencard3.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findUserByEmail(email);
        return convertToResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findUserByEmail(email);

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setBirthday(request.getBirthday());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        return convertToResponse(user);
    }

    public List<UserResponse> getAllUsers(int page, int size, String search) {
        if (search != null && !search.isEmpty()) {
            return userRepository.findByNameContainingOrEmailContaining(
                            search,
                            PageRequest.of(page, size)
                    )
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }
        return userRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return convertToResponse(user);
    }

    @Transactional
    public void updateUserStatus(Long id, boolean active) {
        User user = findUserById(id);
        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void updateUserRole(Long id, String role) {
        User user = findUserById(id);
        if (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN")) {
            throw new CustomException("無效的角色類型", HttpStatus.BAD_REQUEST);
        }
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(
                        "找不到指定用戶",
                        HttpStatus.NOT_FOUND
                ));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(
                        "找不到指定用戶",
                        HttpStatus.NOT_FOUND
                ));
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthday(user.getBirthday())
                .gender(user.getGender())
                .address(user.getAddress())
                .role(user.getRole())
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
}
